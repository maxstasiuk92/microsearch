package microsearch.search.execution.site;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;
import microsearch.search.monitoring.ProblemMonitor;
import microsearch.search.monitoring.RuntimeProblem;

public class RateLimitedHttpProvider implements HttpProvider {
	protected final HttpClient httpClient;
	protected volatile long requestDelay, waitTimeout;
	protected ConcurrentLinkedQueue<Synchronizer> customerQueue;
	protected ProblemMonitor problemMonitor;
		
	public RateLimitedHttpProvider(long requestDelayMillis, long waitTimeoutMillis, long connectTimeoutMillis,
			ProblemMonitor problemMonitor) {
		if (requestDelayMillis <= 0 || waitTimeoutMillis <= 0 || connectTimeoutMillis <= 0) {
			throw new IllegalArgumentException("time can not be negative");
		}
		if (requestDelayMillis > waitTimeoutMillis) {
			throw new IllegalArgumentException();
		}
		this.requestDelay = requestDelayMillis;
		this.waitTimeout = waitTimeoutMillis;
		this.problemMonitor = problemMonitor;
		customerQueue = new ConcurrentLinkedQueue<>();
		httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofMillis(connectTimeoutMillis))
				.followRedirects(Redirect.NEVER)
				.version(Version.HTTP_1_1)
				.build();
		Thread timerThread = new Thread(new HttpRequestTimer());
		timerThread.setDaemon(true);
		timerThread.start();
	}
	
	@Override
	public HttpResponse<InputStream> getResponse(HttpRequest request) {
		boolean sendRequest = false;
		HttpResponse<InputStream> response = null;
		Synchronizer sync = new Synchronizer();
		synchronized (sync) {
			customerQueue.add(sync);
			try {
				sync.wait(waitTimeout);
			} catch (InterruptedException e) {
				sync.state = Synchronizer.OUTDATED;
				throw new RuntimeException(e);
			}
			if (sync.state == Synchronizer.NOTIFIED) {
				sendRequest = true;
			} else {
				sync.state = Synchronizer.OUTDATED;
				customerQueue.remove(sync);
			}
		}
		if (sendRequest) {
			try {
				response = httpClient.send(request, BodyHandlers.ofInputStream());
			} catch (HttpTimeoutException e) {
				//null will be returned
			} catch (InterruptedException | IOException e) {
				throw new RuntimeException(e);
			} finally {
				sync.state = Synchronizer.OUTDATED;
			}
		}
		return response;
	}
	
	protected class Synchronizer {
		static final int WAITING = 0, NOTIFIED = 1, OUTDATED = 2;
		volatile int state = WAITING;
	}
	
	protected class HttpRequestTimer implements Runnable {
		
		@Override
		public void run() {
			Synchronizer sync = null;
			while (true) {
				sync = customerQueue.poll();
				if (sync == null) {
					//queue is empty -> nothing to do
					Thread.yield();
					continue;
				}
				synchronized (sync) {
					if (sync.state == Synchronizer.OUTDATED) {
						//already out-dated -> poll next
						continue;
					}
					sync.state = Synchronizer.NOTIFIED;
					sync.notify();
				}
				//let notified thread to proceed
				Thread.yield();
				int state;
				//wait till notified thread finish request
				do {
					state = sync.state;
					if (state == Synchronizer.OUTDATED) {
						//thread finished request -> sleep to make a delay between requests
						try {
							Thread.sleep(requestDelay);
						} catch (InterruptedException e) {
							problemMonitor.report(new RuntimeProblem(e));
						}
					} else {
						//thread did not finish -> nothing to do
						Thread.yield();
					}
				} while (state != Synchronizer.OUTDATED);
			}
		}
		
	}
}

package microsearch.search.execution.web_search;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import microsearch.search.execution.site.RateLimitedHttpProvider;

import static org.junit.jupiter.api.Assertions.*;


public class RateLimitedRequestorTests {
	public static final boolean PRINT_TEST_QUALITY = true;
	
	public static final String TEST_URL = "https://imrad.com.ua";
	public static final long CONNECTION_TIMEOUT = 5000;
	public static final long REQUEST_DELAY = 1000;
	public static final long REQUEST_DURATION = 200;
	final static int CONCUR_REQUEST_NUMBER = 20;
	
	protected ScheduledExecutorService timerExecutor;
	protected Timer timer;
	
	@BeforeEach
	public void createTimer() {
		timerExecutor = Executors.newSingleThreadScheduledExecutor();
		timer = new Timer();
		timerExecutor.scheduleAtFixedRate(timer, 0, 1, TimeUnit.MILLISECONDS);
	}
	
	@AfterEach
	public void destroyTimer() throws Exception {
		timer.stop();
		timerExecutor.shutdown();
		timerExecutor.awaitTermination(1, TimeUnit.SECONDS);
	}
	
	@Test
	@Timeout(value = CONNECTION_TIMEOUT + REQUEST_DURATION, unit = TimeUnit.MILLISECONDS)
	public void receiveResponseCheck() throws Exception {
		final long waitTimeout = 1000;
		RateLimitedHttpProvider requestor = new RateLimitedHttpProvider(REQUEST_DELAY, waitTimeout, 
				CONNECTION_TIMEOUT, new ProblemLogger());
		HttpRequest request = HttpRequest.newBuilder()
				.uri(new URI(TEST_URL))
				.GET()
				.build();
		timer.activate();
		HttpResponse<InputStream> response = requestor.getResponse(request);
		timer.stop();
		assertTrue(isGoodResponce(response));
	}
	
	@Test
	@Timeout(value = CONCUR_REQUEST_NUMBER * (CONNECTION_TIMEOUT + REQUEST_DURATION + REQUEST_DELAY), unit = TimeUnit.MILLISECONDS)
	public void concurrentRequestsCheck() throws Exception {
		final int actorNumber = 5;
		final long waitTimeout = actorNumber*REQUEST_DELAY/2;
		AtomicInteger requestDownCounter = new AtomicInteger(CONCUR_REQUEST_NUMBER);
		List<Long> timeStamps = Collections.<Long>synchronizedList(new ArrayList<Long>(CONCUR_REQUEST_NUMBER));
		ArrayList<Future<int[]>> actorResults = new ArrayList<>(actorNumber);
		AtomicBoolean startFlag = new AtomicBoolean(false);
		RateLimitedHttpProvider requestor = new RateLimitedHttpProvider(REQUEST_DELAY, waitTimeout/2,
				CONNECTION_TIMEOUT, new ProblemLogger());
		//submit test-actors
		ExecutorService actorExecutor = Executors.newFixedThreadPool(actorNumber);
		for (int i = 0; i < actorNumber; i++) {
			actorResults.add(actorExecutor.submit(new TestActor(requestor, requestDownCounter, timeStamps, waitTimeout, startFlag)));
		}
		//launch test
		timer.activate();
		startFlag.set(true);
		//wait results and check test quality - each actor performed at least 1 request
		int successfulRequests = 0;
		int failedRequests = 0;
		for (Future<int[]> r : actorResults) {
			int s = r.get()[0];
			int f = r.get()[1];
			successfulRequests += s;
			failedRequests += f;
			if (PRINT_TEST_QUALITY) {
				System.out.println("succeeded requests: " + s + ", failed: " + f);
			}
		}
		assertEquals(CONCUR_REQUEST_NUMBER, successfulRequests + failedRequests);
		assertTrue(0.25*CONCUR_REQUEST_NUMBER < successfulRequests && successfulRequests < 0.75*CONCUR_REQUEST_NUMBER);
		assertTrue(0.25*CONCUR_REQUEST_NUMBER < failedRequests && failedRequests < 0.75*CONCUR_REQUEST_NUMBER);
		
		//check time-stamps
		timeStamps.sort(Long::compare);
		System.out.println(timeStamps);
		for (int i = 0; i < timeStamps.size() - 1; i++) {
			long delay = timeStamps.get(i + 1) - timeStamps.get(i);
			assertTrue(delay >= REQUEST_DELAY);
		}
		//clean
		timer.stop();
		actorExecutor.shutdown();
		actorExecutor.awaitTermination(1, TimeUnit.SECONDS);
	}
	
	protected boolean isGoodResponce(HttpResponse<InputStream> response) {
		if (response.statusCode() != 200) {
			return false;
		}
		char[] bodyFragmentBuffer= new char[100];
		int fragmentSize = 0;
		try (Reader bodyReader = new InputStreamReader(response.body())) {
			fragmentSize = bodyReader.read(bodyFragmentBuffer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return fragmentSize > 0;
	}
	
	class TestActor implements Callable<int[]> {
		protected RateLimitedHttpProvider requestor;
		protected AtomicInteger requestDownCounter;
		protected List<Long> timeStamps;
		protected AtomicBoolean startFlag;
		protected long maxSleep;
		
		public TestActor(RateLimitedHttpProvider requestor,
				AtomicInteger requestDownCounter, List<Long> timeStamps, long maxSleep, AtomicBoolean startFlag) {
			this.requestor = requestor;
			this.requestDownCounter = requestDownCounter;
			this.timeStamps = timeStamps;
			this.maxSleep = maxSleep;
			this.startFlag = startFlag;
		}
		
		@Override
		public int[] call() throws Exception {
			int succeeded = 0, failed = 0;
			while (!startFlag.get()) {
				Thread.yield();
			}
			while (requestDownCounter.getAndDecrement() > 0) {
				Thread.sleep((long)(maxSleep*Math.random()));
				HttpRequest request = HttpRequest.newBuilder()
						.uri(new URI(TEST_URL))
						.GET()
						.build();
				HttpResponse<InputStream> response = requestor.getResponse(request);
				long timestamp = timer.getCounter();
				if (response == null) {
					failed++;
				} else {
					succeeded++;
					timeStamps.add(timestamp);
					if (!isGoodResponce(response)) {
						throw new RuntimeException("Received not good response");
					}
				}
			}
			return new int[] {succeeded, failed};
		}
	}
	
	class Timer implements Runnable {
		protected volatile boolean active;
		protected volatile long counter;
		
		public Timer() {
			active = false;
			counter = 0;
		}
		
		synchronized public void activate() {
			if (!active) {
				active = true;
				counter = 0;
			}
		}
		
		synchronized public void stop() {
			active = false;
		}
		
		synchronized public long getCounter() {
			return counter;
		}

		@Override
		synchronized public void run() {
			if (active && counter <= Long.MAX_VALUE) {
				counter++;
			}
		}
	}
}

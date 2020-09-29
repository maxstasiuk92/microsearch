package microsearch.business;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import microsearch.business.entities.Supplier;
import microsearch.search.execution.site.imrad.ImradSearchProcessor;

@Component(value = "supplierRegistry")
@Primary
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class EmbeddedSupplierRegistry extends SupplierRegistry {

	@Override
	protected void loadSupplierRegistry() {
		loadImrad();
		loadKosmodrom();
	}
	
	protected void loadImrad() {
		Supplier s = createSupplier(1, "³לנאה"/*"imrad"*/, "https://imrad.com.ua");
		AssociatedResources r = new AssociatedResources(ImradSearchProcessor.class);
		addToSupplierRegistry(s, r);
	}
	
	protected void loadKosmodrom() {
		//TODO: implement for this and others
	}

}

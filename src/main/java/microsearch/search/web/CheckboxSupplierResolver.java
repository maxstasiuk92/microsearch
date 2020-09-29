package microsearch.search.web;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import microsearch.business.SupplierRegistry;
import microsearch.business.entities.Supplier;
import microsearch.search.exceptions.FetcherException;

@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
@Primary
public class CheckboxSupplierResolver implements SupplierResolver {
	
	private SupplierRegistry supplierRegistry;
	
	@Autowired
	public void setSupplierRegistry(@NonNull SupplierRegistry supplierRegistry) {
		this.supplierRegistry = supplierRegistry;
	}
	
	@Override
	public String[] getHtmlPresentations() {
		Set<Supplier> suppliers = supplierRegistry.getSupplierSet();
		String[] result = new String[suppliers.size()];
		int i = 0;
		for (var s : suppliers) {
			result[i++] = getFormInput(s);
		}
		return result;
	}
	
	@Override
	public Set<Supplier> getRequestedSet(String[] requestedSuppliers) throws FetcherException {
		Set<Supplier> result = new HashSet<>();
		for (var s : requestedSuppliers) {
			try {
				int id = Integer.parseInt(s);
				if (!result.add(supplierRegistry.getSupplier(id))) {
					throw new IllegalArgumentException("not unique supplier with id = " + s);
				}
			} catch(NumberFormatException e) {
				throw new FetcherException("requestedSuppliers contains '" + s + "' which is not Integer");
			}
		}
		return result;
	}
	
	protected String getFormInput(Supplier supplier) {
		final String checkbox = "<input type=\"checkbox\" name=\"suppliers\" value=\"${id}\" checked>${text}";
		final String link = "<a href=\"${link}\" target=\"_blank\">${name}</a>";
		final String name = "${name}";
		
		String text;
		if (supplier.getUrl().isBlank()) {
			text = name.replace("${name}", supplier.getName());
		} else {
			text = link.replace("${name}", supplier.getName()).replace("${link}", supplier.getUrl());
		}
		String result = checkbox.replace("${id}", Integer.toString(supplier.getId())).replace("${text}", text);
		return result;
	}
}

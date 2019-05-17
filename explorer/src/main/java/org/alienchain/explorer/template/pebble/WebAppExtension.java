package org.alienchain.explorer.template.pebble;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;

import java.util.HashMap;
import java.util.Map;

public class WebAppExtension extends AbstractExtension{

	
	@Override
	public Map<String, Filter> getFilters() {
		
		HashMap<String,Filter> map = new HashMap<>();
		
		map.put("suntrx", new SunTrx());
		map.put("numberlocale", new NumberLocale());
		
		return map;
	}
	
}

package com.proxy.model.option;

import java.util.ArrayList;
import java.util.List;

/** Clase que representa las opciones de cabeceras User-Agent por defecto.
 * @author Pablo
 *
 */
public class DefaultOptionUserAgent extends AbstractDefaultOption {

	public DefaultOptionUserAgent(String filePath) {
		super(filePath);
	}

	@Override
	List<Option> loadOptions() {
		List<Option> options = new ArrayList<>();

		options.add(new OptionUserAgent(
				"Android $$ Google Chrome >> Mozilla/5.0 (Linux; Android 8.0.0; TA-1053 Build/OPR1.170623.026) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3368.0 Mobile Safari/537.36"));
		options.add(new OptionUserAgent(
				"Android $$ Mozilla Firefox >> Mozilla/5.0 (Android 8.1.0; Mobile; rv:61.0) Gecko/61.0 Firefox/61.0"));
		options.add(new OptionUserAgent(
				"Android $$ Opera >> Mozilla/5.0 (Linux; U; Android 8.0.0; Pixel XL Build/OPR6.170623.012; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/59.0.3071.125 Mobile Safari/537.36 OPR/28.0.2254.119213"));
		options.add(new OptionUserAgent(
				"Android $$ Microsoft Edge >> Mozilla/5.0 (Linux; Android 8.0; Pixel XL Build/OPP3.170518.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.0 Mobile Safari/537.36 EdgA/41.1.35.1"));
		options.add(new OptionUserAgent(
				"Windows $$ Google Chrome >> Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36"));
		options.add(new OptionUserAgent(
				"Windows $$ Mozilla Firefox >> Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0"));
		options.add(new OptionUserAgent(
				"Windows $$ Opera >> Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36 OPR/43.0.2442.991"));
		options.add(new OptionUserAgent(
				"Windows $$ Safari >> Mozilla/5.0 (Windows; U; Windows NT 10.0; en-US) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/11.0 Safari/603.1.30"));
		options.add(new OptionUserAgent(
				"Windows $$ Microsoft Edge >> Mozilla/5.0 (Windows NT 10.0; Win64; x64; ServiceUI 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36 Edge/18.18362"));

		return options;
	}

}

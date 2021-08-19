package lu.uni.trux.IfExtractor;

import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.options.Options;

/**
 * FlowDroid config
 * @author François JULLION
 */
public class MyConfig implements IInfoflowConfig {
	
	@SuppressWarnings("static-access")
	@Override
	public void setSootOptions(Options options, InfoflowConfiguration config) {
		String dirOutput = System.getProperty("user.dir") + "/output"; 
		options.v().set_output_format(Options.output_format_dex);
		options.v().set_output_dir(dirOutput);

	}

}

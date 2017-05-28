package test.henshin;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;

import solution.HenshinSolution;
import test.TestFramework;
import transitiongraph.TransitionGraph;



public class HenshinRunner extends TestFramework {

	public static void main(String[] args) throws IOException {
		HenshinRunner runner = new HenshinRunner();
		println("Starting evaluation of main task");
		runner.testFSAToRegexAllModels();
		println("Starting evaluation of extension 1");
		runner.testSimplifyFSAAllModels();
		println("Starting evaluation of extension 2");
		runner.testDTMCToSREAllModels();
		println("Done with entire evaluation.");
		System.exit(0);
	}

	@Override
	public String FSAToRegex(TransitionGraph fsa) {
		URI uri = fsa.eResource().getURI();
		String result = HenshinSolution.convertFSAToRegExp(uri);
		return result;
	}

	@Override
	public String DTMCToSRE(TransitionGraph dtmc) {
		URI uri = dtmc.eResource().getURI();
		String result = HenshinSolution.convertPAToRegExp(uri);
		return result;
	}

	@Override
	public TransitionGraph simplifyFSA(TransitionGraph tg) {
		URI uri = tg.eResource().getURI();
		TransitionGraph result = HenshinSolution.convertToSimpleAutomaton(uri);
		return result;
	}

	private static void println(String string) {
		System.out.println();
		System.out.println("==========================");
		System.out.println(string);
		System.out.println("==========================");
	}
	
}

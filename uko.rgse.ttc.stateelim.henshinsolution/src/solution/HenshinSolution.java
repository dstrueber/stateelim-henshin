package solution;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

import transitiongraph.State;
import transitiongraph.Transition;
import transitiongraph.TransitionGraph;
import transitiongraph.TransitiongraphPackage;

public class HenshinSolution {
	private static boolean LOGGING = false;
	private static String FILE_PATH_RSA2REGEX = "transformation\\rsa2regex.henshin";
	private static String FILE_PATH_PA2REGEX = "transformation\\pa2regex.henshin";

	public static String convertFSAToRegExp(URI uri) {
		return convertToRegExp(uri, FILE_PATH_RSA2REGEX);
	}
	
	public static String convertPAToRegExp(URI uri) {
		return convertToRegExp(uri, FILE_PATH_PA2REGEX);
	}
	
	private static String convertToRegExp(URI uri, String modulePath) {
		HenshinResourceSet resourceSet = initResourceSet();
		Module module = resourceSet.getModule(modulePath, false);
		TransitionGraph tg = (TransitionGraph) resourceSet.getResource(uri, true).getContents().get(0);
		UnitApplication unitApplication = new UnitApplicationImpl(new EngineImpl(), new EGraphImpl(tg),
				module.getUnit("main"), null);

		printInfo(tg);
		if (!unitApplication.execute(null)) {
			throw new RuntimeException("Error executing unit");
		}

		if (modulePath.equals(FILE_PATH_PA2REGEX))
			postprocess(tg);
		printInfo(tg);

		return getRegExpression(tg);
	}

	public static TransitionGraph convertToSimpleAutomaton(URI uri) {
		HenshinResourceSet resourceSet = initResourceSet();
		Module module = resourceSet.getModule("transformation\\simplifyrsa.henshin", false);
		TransitionGraph tg = (TransitionGraph) resourceSet.getResource(uri, true).getContents().get(0);
		UnitApplication unitApplication = new UnitApplicationImpl(new EngineImpl(), new EGraphImpl(tg),
				module.getUnit("main"), null);

		// printInfo(tg);
		if (!unitApplication.execute(null)) {
			throw new RuntimeException("Error executing unit");
		}
		// printInfo(tg);

		return tg;
	}

	private static HenshinResourceSet initResourceSet() {
		HenshinResourceSet resourceSet = new HenshinResourceSet("");
		resourceSet.getPackageRegistry().put(TransitiongraphPackage.eNS_URI, TransitiongraphPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		return resourceSet;
	}

	private static Transition getFirstOrNull(List<Transition> collect) {
		if (collect.isEmpty())
			return null;
		else
			return collect.get(0);
	}

	private static String getRegExpression(TransitionGraph tg) {
		Transition ii_ = getFirstOrNull(tg.getTransitions().stream()
				.filter(x -> x.getSource().isIsInitial() && x.getTarget().isIsInitial()).collect(Collectors.toList()));
		Transition if_ = getFirstOrNull(tg.getTransitions().stream()
				.filter(x -> x.getSource().isIsInitial() && x.getTarget().isIsFinal()).collect(Collectors.toList()));
		Transition fi_ = getFirstOrNull(tg.getTransitions().stream()
				.filter(x -> x.getSource().isIsFinal() && x.getTarget().isIsInitial()).collect(Collectors.toList()));
		Transition ff_ = getFirstOrNull(tg.getTransitions().stream()
				.filter(x -> x.getSource().isIsFinal() && x.getTarget().isIsFinal()).collect(Collectors.toList()));

		StringBuilder result = new StringBuilder();
		result.append('(');
		if (ii_ != null) {
			result.append('(');
			result.append((ii_.getLabel()));
			result.append(')');
			result.append('*');
		}
		if (if_ != null) {
			result.append('(');
			result.append((if_.getLabel()));
			result.append(')');
		}
		if (ff_ != null) {
			result.append('(');
			result.append((ff_.getLabel()));
			result.append(')');
			result.append('*');
		}
		if (fi_ != null) {
			result.append('(');
			result.append((fi_.getLabel()));
			result.append(')');
		}
		if (result.toString().length() == 1) {
			result = new StringBuilder();
		} else {
			result.append(')');
			result.append('*');
		}

		if (ii_ != null) {
			result.append('(');
			result.append((ii_.getLabel()));
			result.append(')');
			result.append('*');
		}
		if (if_ != null) {
			result.append('(');
			result.append((if_.getLabel()));
			result.append(')');
		}
		if (ff_ != null) {
			result.append('(');
			result.append((ff_.getLabel()));
			result.append(')');
			result.append('*');
		}
		return result.toString();
	}

	@SuppressWarnings("unused")
	private static void checkIsSimple(TransitionGraph tg) {
		Set<State> initial = tg.getStates().stream().filter(x -> x.isIsInitial()).collect(Collectors.toSet());
		Set<State> fin = tg.getStates().stream().filter(x -> x.isIsFinal()).collect(Collectors.toSet());
		if (initial.size() != 1) {
			throw new RuntimeException("Found " + initial.size() + " initial states!");
		}
		if (fin.size() != 1) {
			throw new RuntimeException("Found " + fin.size() + " final states!");
		}
	}

	private static void printInfo(TransitionGraph graph) {
		if (LOGGING) {
			System.out.println(" ===");
			System.out.println("States [n=" + graph.getStates().size() + ", i="
					+ graph.getStates().stream().filter(x -> x.isIsInitial()).collect(Collectors.toList()).size()
					+ ", f=" + graph.getStates().stream().filter(x -> x.isIsFinal()).collect(Collectors.toList()).size()
					+ "]: " + graph.getStates().stream().map(x -> x.getId() + "").collect(Collectors.joining(",")));
			System.out.println("Transitions [n=" + graph.getTransitions().size() + "]: "
					+ graph.getTransitions().stream()
							.map(x -> x.getSource().getId() + "->" + x.getTarget().getId() + "[" + x.getLabel() + "]")
							.collect(Collectors.joining(",")));
		}
	}

	private static void postprocess(TransitionGraph tg) {
		for (Transition transition : tg.getTransitions()) {
			if (transition.getLabel() != null) {
				String label = transition.getLabel();
				Pattern regex = Pattern.compile("\\[([\\d|\\.]+)\\]\\[([\\d|\\.]+)\\]");
				Matcher regexMatcher = regex.matcher(label);
				while (regexMatcher.find()) {
					double num1 = Double.parseDouble(regexMatcher.group(1));
					double num2 = Double.parseDouble(regexMatcher.group(2));
					String newLabel = regexMatcher.replaceAll("[" + (num1 * num2) + "]");
					label = newLabel;
				}
				transition.setLabel(label);

			}
		}
	}
}

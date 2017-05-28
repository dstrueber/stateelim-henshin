package test.henshin;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.eclipse.emf.henshin.trace.Trace;
import org.eclipse.emf.henshin.trace.TracePackage;

import transitiongraph.State;
import transitiongraph.Transition;
import transitiongraph.TransitionGraph;
import transitiongraph.TransitiongraphPackage;
import transitiongraph.impl.TransitiongraphFactoryImpl;


public class HenshinToyExample {
	public static void main(String[] args) {
		HenshinResourceSet resourceSet = new HenshinResourceSet("");
		resourceSet.getPackageRegistry().put(TransitiongraphPackage.eNS_URI, TransitiongraphPackage.eINSTANCE);
		resourceSet.getPackageRegistry().put(TracePackage.eNS_URI, TracePackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

		Module module = resourceSet.getModule("transformation\\pa2regex.henshin", false);
		
		TransitionGraph tg = TransitiongraphFactoryImpl.eINSTANCE.createTransitionGraph();
		State s0 = TransitiongraphFactoryImpl.eINSTANCE.createState();
		State s1 = TransitiongraphFactoryImpl.eINSTANCE.createState();
		State s2 = TransitiongraphFactoryImpl.eINSTANCE.createState();
		s0.setId(0);
		s1.setId(1);
		s2.setId(2);
		tg.getStates().add(s0);
		tg.getStates().add(s1);
		tg.getStates().add(s2);
		Transition t0 =  TransitiongraphFactoryImpl.eINSTANCE.createTransition();
		Transition t1 =  TransitiongraphFactoryImpl.eINSTANCE.createTransition();
		t0.setLabel("a");
		t0.setSource(s0);
		t0.setTarget(s1);
		t1.setLabel("b");
		t1.setSource(s0);
		t1.setTarget(s2);
		t0.setProbability(0.2);
		t1.setProbability(0.5);
		tg.getTransitions().add(t0);
		tg.getTransitions().add(t1);
		convertToRegExp(tg, module);
		
	}

	public static void  convertToRegExp(TransitionGraph tg, Module module) {
		EGraph graph = new EGraphImpl(tg);

		Engine engine = new EngineImpl();
		engine.getOptions().put(Engine.OPTION_SORT_VARIABLES, true);
		engine.getOptions().put(Engine.OPTION_INVERSE_MATCHING_ORDER, true);
		printInfo(tg, graph);
		apply(module, graph, engine, "recalculateProbabilities");
		printInfo(tg, graph);

	}

	

	private static void apply(Module module, EGraph graph, Engine engine, String unitName) {
		UnitApplication unitApp = new UnitApplicationImpl(engine);
		unitApp.setEGraph(graph);
		unitApp.setUnit(module.getUnit(unitName));
		if (!unitApp.execute(null)) {
			throw new RuntimeException("Error executing unit");
		}
	}

	static void printInfo(TransitionGraph tg, EGraph graph) {
		System.out.println("States: "+tg.getStates().stream().map(x -> x.getId()+"").collect(Collectors.joining(",")));
		System.out.println("Transitions: "+tg.getTransitions().stream().map(x -> x.getSource().getId()+"->"+x.getTarget().getId()+"["+x.getLabel()+";"+x.getProbability()+"]").collect(Collectors.joining(",")));
		System.out.println("Traces: "+graph.getRoots().stream().filter(x -> x instanceof Trace).map(x -> ((Trace)x).getName()).collect(Collectors.joining(",")));
	}
}

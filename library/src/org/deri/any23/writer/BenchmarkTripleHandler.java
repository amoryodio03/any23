package org.deri.any23.writer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.deri.any23.extractor.ExtractionContext;

import com.hp.hpl.jena.graph.Node;


public class BenchmarkTripleHandler implements TripleHandler {
	private TripleHandler underlyingHandler;

	private class StatObject{
		int methodCalls = 0;
		int triples = 0;
		long runtime =0;
		long intStart =0;
		/**
		 * 
		 */
		public void interimStart() {
			intStart = System.currentTimeMillis();
		}
		/**
		 * 
		 */
		public void interimStop() {
			runtime+=(System.currentTimeMillis()-intStart);
			intStart=0;
		}
	}
	
	private final Map<String,StatObject> stats;
	
	/**
	 * 
	 */
	public BenchmarkTripleHandler(TripleHandler tripleHandler) {
		underlyingHandler = tripleHandler;
		stats = new HashMap<String, StatObject>();
		stats.put("SUM", new StatObject());
	}

	/* (non-Javadoc)
	 * @see org.deri.any23.writer.TripleHandler#close()
	 */
	public void close() {
		underlyingHandler.close();
	}

	/* (non-Javadoc)
	 * @see org.deri.any23.writer.TripleHandler#closeContext(org.deri.any23.extractor.ExtractionContext)
	 */
	public void closeContext(ExtractionContext context) {
		if(!stats.containsKey(context.getExtractorName())){stats.put(context.getExtractorName(), new StatObject());}
		stats.get(context.getExtractorName()).interimStop();
		stats.get("SUM").interimStop();
		underlyingHandler.closeContext(context);
		
	}

	/* (non-Javadoc)
	 * @see org.deri.any23.writer.TripleHandler#openContext(org.deri.any23.extractor.ExtractionContext)
	 */
	public void openContext(ExtractionContext context) {
		if(!stats.containsKey(context.getExtractorName())){stats.put(context.getExtractorName(), new StatObject());}
		stats.get(context.getExtractorName()).methodCalls++;
		stats.get(context.getExtractorName()).interimStart();
		stats.get("SUM").methodCalls++;
		stats.get("SUM").interimStart();
		underlyingHandler.openContext(context);
	}

	/* (non-Javadoc)
	 * @see org.deri.any23.writer.TripleHandler#receiveLabel(java.lang.String, org.deri.any23.extractor.ExtractionContext)
	 */
	public void receiveLabel(String label, ExtractionContext context) {
		underlyingHandler.receiveLabel(label, context);
	}

	/* (non-Javadoc)
	 * @see org.deri.any23.writer.TripleHandler#receiveTriple(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, org.deri.any23.extractor.ExtractionContext)
	 */
	public void receiveTriple(Node s, Node p, Node o, ExtractionContext context) {
		if(!stats.containsKey(context.getExtractorName())){stats.put(context.getExtractorName(), new StatObject());}
		stats.get(context.getExtractorName()).triples++;
		stats.get("SUM").triples++;
		underlyingHandler.receiveTriple(s, p, o, context);
	}

	/**
	 * @return
	 */
	public String report() {
		StringBuilder sb = new StringBuilder();
		StatObject sum = stats.get("SUM");
		
		sb.append("\n>Summary: ");
		sb.append("\n   -total calls: ").append(sum.methodCalls);
		sb.append("\n   -total triples: ").append(sum.triples);
		sb.append("\n   -total runtime: ").append(sum.runtime).append(" ms!");
		if(sum.runtime != 0)
		sb.append("\n   -tripls/ms: ").append(sum.triples/sum.runtime);
		if(sum.methodCalls != 0)
		sb.append("\n   -ms/calls: ").append(sum.runtime/sum.methodCalls);

		stats.remove("SUM");
		
		for(Entry<String, StatObject>ent: stats.entrySet()) {
			sb.append("\n>Extractor: ").append(ent.getKey());
			sb.append("\n   -total calls: ").append(ent.getValue().methodCalls);
			sb.append("\n   -total triples: ").append(ent.getValue().triples);
			sb.append("\n   -total runtime: ").append(ent.getValue().runtime).append(" ms!");
			if(ent.getValue().runtime != 0)
			sb.append("\n   -tripls/ms: ").append(ent.getValue().triples/ent.getValue().runtime);
			if(ent.getValue().methodCalls != 0)
			sb.append("\n   -ms/calls: ").append(ent.getValue().runtime/ent.getValue().methodCalls);
			
		}
		
		return sb.toString();
	}
}
package org.icgc.dcc.sodalite.server.service;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.Analysis;
import org.icgc.dcc.sodalite.server.model.json.register.SequencingReadSubmission;
import org.icgc.dcc.sodalite.server.model.json.register.VariantCallSubmission;
import org.icgc.dcc.sodalite.server.model.json.update.analysis.SequencingReadUpdate;
import org.icgc.dcc.sodalite.server.model.json.update.analysis.VariantCallUpdate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor

public class AnalysisService {
	private void info(String fmt, Object... args) {
		log.info(format(fmt, args));
	}


	public List<Analysis> getAnalysisById(String id) {
		// TODO Auto-generated method stub
		info("Called GetAnalysisById with %s",id);
		return null;
	}

	public List<Analysis> getAnalyses(Map<String, String> params) {
		info("Called getAnalyses with %s",params);
		// TODO Auto-generated method stub
		return null;
	}

	public int registerSequencingRead(SequencingReadSubmission sequencingReadSubmission) {
		// TODO Auto-generated method stub
		
		return 0;
	}
	public int registerVariantCall(VariantCallSubmission variantCallSubmission) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int updateSequencingRead(SequencingReadUpdate sequencingReadUpdate) {
		// TODO Auto-generated method stub
		return 0;
		
	}


	public int updateVariantCall(VariantCallUpdate variantCallUpdate) {
		return 0;
		
	}

}

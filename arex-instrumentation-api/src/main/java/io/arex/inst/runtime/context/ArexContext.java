package io.arex.inst.runtime.context;

import io.arex.agent.bootstrap.util.ConcurrentHashSet;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.MergeDTO;
import io.arex.inst.runtime.util.MergeRecordReplayUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ArexContext {

    private final String caseId;
    private final String replayId;
    private final long createTime;
    private final AtomicInteger sequence;
    private Set<Integer> methodSignatureHashList;
    private Map<Integer, List<MergeDTO>> cachedReplayResultMap;
    private Map<String, Set<String>> excludeMockTemplate;

    private Map<String, Object> attachments = null;

    private LinkedBlockingQueue<MergeDTO> mergeRecordQueue;

    private boolean isRedirectRequest;
    private boolean isInvalidCase;

    public static ArexContext of(String caseId) {
        return of(caseId, null);
    }

    public static ArexContext of(String caseId, String replayId) {
        return new ArexContext(caseId, replayId);
    }

    private ArexContext(String caseId, String replayId) {
        this.createTime = System.currentTimeMillis();
        this.caseId = caseId;
        this.sequence = new AtomicInteger(0);
        this.replayId = replayId;
    }

    public String getCaseId() {
        return this.caseId;
    }

    public String getReplayId() {
        return this.replayId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public boolean isRecord() {
        return !isReplay();
    }

    public boolean isReplay() {
        return StringUtil.isNotEmpty(this.replayId);
    }

    public int calculateSequence() {
        return sequence.getAndIncrement();
    }

    public Set<Integer> getMethodSignatureHashList() {
        if (methodSignatureHashList == null) {
            methodSignatureHashList = new ConcurrentHashSet<>();
        }
        return methodSignatureHashList;
    }

    public Map<Integer, List<MergeDTO>> getCachedReplayResultMap() {
        if (cachedReplayResultMap == null) {
            cachedReplayResultMap = new ConcurrentHashMap<>();
        }
        return cachedReplayResultMap;
    }
    public Map<String, Set<String>> getExcludeMockTemplate() {
        return excludeMockTemplate;
    }

    public void setExcludeMockTemplate(Map<String, Set<String>> excludeMockTemplate) {
        this.excludeMockTemplate = excludeMockTemplate;
    }

    public void setAttachment(String key, Object value) {
        if (attachments == null) {
            attachments = new HashMap<>();
        }

        attachments.put(key, value);
    }

    public Object getAttachment(String key) {
        if (attachments == null) {
            return null;
        }

        return attachments.get(key);
    }

    public boolean isRedirectRequest() {
        return isRedirectRequest;
    }

    public void setRedirectRequest(boolean redirectRequest) {
        isRedirectRequest = redirectRequest;
    }

    public boolean isInvalidCase() {
        return isInvalidCase;
    }

    public void setInvalidCase(boolean invalidCase) {
        isInvalidCase = invalidCase;
    }

    public boolean isRedirectRequest(String referer) {
        if (attachments == null) {
            isRedirectRequest = false;
            return false;
        }

        if (StringUtil.isEmpty(referer)) {
            isRedirectRequest = false;
            return false;
        }

        Object location = attachments.get(ArexConstants.REDIRECT_REFERER);
        if (location == null) {
            isRedirectRequest = false;
            return false;
        }
        isRedirectRequest = Objects.equals(location, referer);
        return isRedirectRequest;
    }

    public LinkedBlockingQueue<MergeDTO> getMergeRecordQueue() {
        if (mergeRecordQueue == null) {
            mergeRecordQueue = new LinkedBlockingQueue<>(2048);
        }
        return mergeRecordQueue;
    }

    public void clear() {
        if (methodSignatureHashList != null) {
            methodSignatureHashList.clear();
        }
        if (cachedReplayResultMap != null) {
            cachedReplayResultMap.clear();
        }
        if (excludeMockTemplate != null) {
            excludeMockTemplate.clear();
        }
        if (attachments != null) {
            attachments.clear();
        }
        if (mergeRecordQueue != null) {
            // async thread merge record (main entry has ended)
            MergeRecordReplayUtil.recordRemain(this);
            mergeRecordQueue.clear();
        }
    }
}

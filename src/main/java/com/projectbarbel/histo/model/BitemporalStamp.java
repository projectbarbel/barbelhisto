package com.projectbarbel.histo.model;

import java.util.Objects;

import com.projectbarbel.histo.BarbelHistoContext;

public final class BitemporalStamp {

    protected final Object versionId;
    protected final Object documentId;
    protected final String activity;
    protected final EffectivePeriod effectiveTime;
    protected final RecordPeriod recordTime;

    private BitemporalStamp(Builder builder) {
        this.versionId = builder.versionId != null ? builder.versionId : BarbelHistoContext.getDefaultVersionIDGenerator().get();
        this.documentId = builder.documentId != null ? builder.documentId : BarbelHistoContext.getDefaultDocumentIDGenerator().get();
        this.activity = builder.activity != null ? builder.activity : BarbelHistoContext.getDefaultActivity();
        this.effectiveTime = Objects.requireNonNull(builder.effectiveTime);
        this.recordTime = Objects.requireNonNull(builder.recordTime);
    }

    public static BitemporalStamp defaultValues() {
        return builder().withActivity(BarbelHistoContext.getDefaultActivity())
                .withDocumentId((String) BarbelHistoContext.getDefaultDocumentIDGenerator().get())
                .withVersionId(BarbelHistoContext.getDefaultVersionIDGenerator().get())
                .withEffectiveTime(EffectivePeriod.builder().build()).withRecordTime(RecordPeriod.builder().build())
                .build();
    }

    public static BitemporalStamp of(String activity, Object documentId, EffectivePeriod effectiveTime,
            RecordPeriod recordTime) {
        return builder().withActivity(activity).withDocumentId(documentId).withEffectiveTime(effectiveTime)
                .withRecordTime(recordTime).withVersionId(BarbelHistoContext.getDefaultVersionIDGenerator().get())
                .build();
    }

    public Object getVersionId() {
        return versionId;
    }

    public EffectivePeriod getEffectiveTime() {
        return effectiveTime;
    }

    public RecordPeriod getRecordTime() {
        return recordTime;
    }

    public Object getDocumentId() {
        return documentId;
    }

    public String getActivity() {
        return activity;
    }

    public BitemporalStamp inactivatedCopy(String inactivatedBy) {
        return builder().withActivity(activity).withDocumentId(documentId).withEffectiveTime(effectiveTime)
                .withRecordTime(recordTime.inactivate(inactivatedBy)).withVersionId(versionId).build();
    }

    public boolean isActive() {
        return recordTime.getState().equals(BitemporalObjectState.ACTIVE);
    }

    @Override
    public String toString() {
        return "BitemporalStamp [versionId=" + versionId + ", documentId=" + documentId + ", activity=" + activity
                + ", effectiveTime=" + effectiveTime + ", recordTime=" + recordTime + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(activity, documentId, effectiveTime, recordTime, versionId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BitemporalStamp)) {
            return false;
        }
        BitemporalStamp other = (BitemporalStamp) obj;
        return Objects.equals(activity, other.activity) && Objects.equals(documentId, other.documentId)
                && Objects.equals(effectiveTime, other.effectiveTime) && Objects.equals(recordTime, other.recordTime)
                && Objects.equals(versionId, other.versionId);
    }

    /**
     * Creates builder to build {@link BitemporalStamp}.
     * 
     * @return created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link BitemporalStamp}.
     */
    public static final class Builder {
        private Object versionId;
        private Object documentId;
        private String activity;
        private EffectivePeriod effectiveTime;
        private RecordPeriod recordTime;

        private Builder() {
        }

        public Builder withVersionId(Object versionId) {
            this.versionId = versionId;
            return this;
        }

        public Builder withDocumentId(Object documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder withActivity(String activity) {
            this.activity = activity;
            return this;
        }

        public Builder withEffectiveTime(EffectivePeriod effectiveTime) {
            this.effectiveTime = effectiveTime;
            return this;
        }

        public Builder withRecordTime(RecordPeriod recordTime) {
            this.recordTime = recordTime;
            return this;
        }

        public BitemporalStamp build() {
            return new BitemporalStamp(this);
        }
    }

}

package edu.cuny.hunter.streamrefactoring.core.analysis;

public enum PreconditionFailure {
	INCONSISTENT_POSSIBLE_STREAM_SOURCE_ORDERING(1),
	NON_ITERABLE_POSSIBLE_STREAM_SOURCE(2),
	NON_INSTANTIABLE_POSSIBLE_STREAM_SOURCE(3),
	NON_DETERMINABLE_STREAM_SOURCE_ORDERING(4), 
	NON_DETERMINABLE_REDUCTION_ORDERING(5), 
	INCONSISTENT_POSSIBLE_EXECUTION_MODES(6);

	private int code;

	private PreconditionFailure(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}

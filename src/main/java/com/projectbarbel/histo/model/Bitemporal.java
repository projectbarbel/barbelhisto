package com.projectbarbel.histo.model;

/**
 * Value Objects in the application must implement this interface.
 * 
 * @author niklasschlimm
 *
 * @param <O> the unique object identifier type of the value object
 */
public interface Bitemporal<O> {

	BitemporalStamp getBitemporalStamp();
	O getObjectId();
	
}

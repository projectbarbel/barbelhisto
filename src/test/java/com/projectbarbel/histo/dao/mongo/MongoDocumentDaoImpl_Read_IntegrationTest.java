package com.projectbarbel.histo.dao.mongo;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelHistoFactory.FactoryType;
import com.projectbarbel.histo.BarbelHistoOptions;

import io.github.benas.randombeans.api.EnhancedRandom;

public class MongoDocumentDaoImpl_Read_IntegrationTest {

	private static MongoDocumentDaoImpl dao;
	
    @BeforeClass
    public static void beforeClass() {
        BarbelHistoOptions opts = BarbelHistoOptions.builder().withDaoSupplierClassName("com.projectbarbel.histo.dao.mongo.FlapDoodleEmbeddedMongoClientDaoSupplier").withServiceSupplierClassName("").build();
        dao = BarbelHistoFactory.createProduct(FactoryType.DAO, opts);
    }

	@Before
	public void setUp() {
	    dao.reset();
	}

	@Test
	public void testReadDocument() {
		DefaultMongoValueObject object = EnhancedRandom.random(DefaultMongoValueObject.class);
		dao.createDocument(object);
		assertTrue("document should be present",dao.readDocument(object.getVersionId()).isPresent());
	}

}

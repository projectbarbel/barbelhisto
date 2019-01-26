package com.projectbarbel.histo.dao.mongo;

import static org.junit.Assert.assertNotNull;

import java.util.NoSuchElementException;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelHistoFactory.HistoType;
import com.projectbarbel.histo.BarbelHistoOptions;
import com.projectbarbel.histo.BarbelTestHelper;
import com.projectbarbel.histo.dao.DocumentDao;

public class MongoDocumentDaoImpl_Reset_IntegrationTest {

	private static DocumentDao<DefaultMongoValueObject, ObjectId> dao;

    @BeforeClass
    public static void beforeClass() {
        BarbelHistoOptions.ACTIVE_CONFIG = BarbelHistoOptions.builder().withDefaultValues().withDaoClassName("com.projectbarbel.histo.dao.mongo.MongoDocumentDaoImpl").build();
        dao = BarbelHistoFactory.instanceOf(HistoType.DAO, new Object[] {FlapDoodleEmbeddedMongoClientDaoSupplier.MONGOCLIENT.getMongo(), "test", "testCol"});
    }

	@Before
	public void setUp() {
	    dao.reset();
	}

	@Test(expected = NullPointerException.class)
	public void testCreateDocument_null() {
		dao.createDocument(null);
	}

	@Test(expected=NoSuchElementException.class)
	public void testCreateDocument_andReset() {
		DefaultMongoValueObject object = BarbelTestHelper.random(DefaultMongoValueObject.class);
		dao.createDocument(object);
		DefaultMongoValueObject doc = dao.readDocument(object.getVersionId()).get();
		assertNotNull(doc);
		dao.reset();
		dao.readDocument(object.getVersionId()).get();
	}

}
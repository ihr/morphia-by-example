package org.ingini.mongodb.morphia.example.read;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.mapping.MappedClass;
import com.google.code.morphia.query.Query;
import com.google.common.collect.Lists;
import com.mongodb.*;
import org.ingini.mongodb.morphia.example.model.heroes.Address;
import org.ingini.mongodb.morphia.example.model.heroes.Heroine;
import org.ingini.mongodb.morphia.example.model.heroes.Human;
import org.ingini.mongodb.morphia.example.model.heroes.Region;
import org.ingini.mongodb.morphia.example.model.weapons.Weapon;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Pattern;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Copyright (c) 2013 Ivan Hristov
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class TestRead {

    public static final String DB_NAME = "morphia_by_example";
    private static Mongo mongo;
    private static DB db;
    private static Datastore ds;
    private static final Morphia morphia = new Morphia();

    @BeforeClass
    public static void beforeClass() throws UnknownHostException {
        mongo = new MongoClient("127.0.0.1", 27017);
        db = mongo.getDB(DB_NAME);
        ds = morphia.createDatastore(mongo, db.getName());
    }

    private static void cleanup() {
        for (final MappedClass mc : morphia.getMapper().getMappedClasses()) {
            db.getCollection(mc.getCollectionName()).drop();
        }

    }

    @AfterClass
    public static void afterClass() {
        cleanup();
        mongo.close();
    }

    @Test
    public void shouldFindSingleDocumentByObjectId() {
        //GIVEN
        Heroine aryaStark = Heroine.createHeroineWithoutChildrenAndNoBeasts("Arya", "Stark", //
                new Address("Winterfell", "Westeros", Region.THE_NORTH));
        Key<Heroine> documentKey = ds.save(aryaStark);

        //WHEN
        Query<Human> result = ds.find(Human.class).field("id").equal(documentKey.getId());

        //THEN
        assertThat(result.get()).isEqualTo(aryaStark);
    }

    @Test
    public void shouldFindWithRegexOperator() {
        //GIVEN
        ds.save(new Weapon("Lightbringer", null, null));

        ds.save(new Weapon("Longclaw", "Valyrian steel", null));
        ds.save(new Weapon("Dark Sister", "Valyrian steel", null));
        ds.save(new Weapon("Ice", "Valyrian steel", null));

        //WHEN
        Pattern regexp = Pattern.compile("steel.*");
        List<Weapon> result = Lists.newArrayList(ds.createQuery(Weapon.class).filter("material", regexp));

        //THEN
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(3);
    }
}
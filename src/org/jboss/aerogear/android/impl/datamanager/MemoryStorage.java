/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.aerogear.android.impl.datamanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.datamanager.IdGenerator;
import org.jboss.aerogear.android.datamanager.Store;
import org.jboss.aerogear.android.datamanager.StoreType;
import org.jboss.aerogear.android.impl.reflection.Property;
import org.jboss.aerogear.android.impl.reflection.Scan;
import org.json.JSONObject;

/**
 * Memory implementation of Store {@link Store}.
 */
public class MemoryStorage<T> implements Store<T> {

    private final Map<Serializable, T> data = new HashMap<Serializable, T>();
    private final IdGenerator idGenerator;

    public MemoryStorage(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoreType getType() {
        return StoreTypes.MEMORY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<T> readAll() {
        return data.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T read(Serializable id) {
        return data.get(id);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void save(T item) {

        String recordIdFieldName = Scan.recordIdFieldNameIn(item.getClass());

        Property property = new Property(item.getClass(), recordIdFieldName);

        Serializable idValue = (Serializable) property.getValue(item);

        if (idValue == null) {
            idValue = idGenerator.generate();
            property.setValue(item, idValue);
        }

        data.put(idValue, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        data.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Serializable id) {
        data.remove(id);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException if filter.query has nested objects
     */
    @Override
    public List<T> readWithFilter(ReadFilter filter) {
        if (filter == null) {
            filter = new ReadFilter();
        }
        JSONObject where = filter.getWhere();
        scanForNestedObjectsInWhereClause(where);
        List<T> results = new ArrayList<T>(data.values());

        filterData(results, where);
        results = pageData(results, filter.getLimit(), filter.getOffset());
        return results;
    }

    private void scanForNestedObjectsInWhereClause(JSONObject where) {
        String key;
        Object value;
        Iterator keys = where.keys();
        while (keys.hasNext()) {
            key = keys.next().toString();
            value = where.opt(key);
            if (value instanceof JSONObject) {
                throw new IllegalArgumentException("readWithFilter does not support nested objects");
            }
        }
    }

    private void filterData(Collection<T> data, JSONObject where) {
        String filterPropertyName;
        Object filterValue;
        Iterator keys = where.keys();
        while (keys.hasNext()) {
            filterPropertyName = keys.next().toString();
            filterValue = where.opt(filterPropertyName);

            for (T objectInStorage : data) {
                Property objectProperty = new Property(objectInStorage.getClass(), filterPropertyName);
                Object propertyValue = objectProperty.getValue(objectInStorage);
                if (propertyValue != null && filterValue != null) {
                    if (!propertyValue.equals(filterValue)) {
                        data.remove(objectInStorage);
                    }
                }
            }
        }
    }

    private List<T> pageData(List<T> results, Integer limit, Integer offset) {
        return results.subList(offset, Math.min(offset + limit, results.size()));
    }
}

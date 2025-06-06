/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.data.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.function.SerializablePredicate;

public abstract class DataProviderTestBase<D extends DataProvider<StrBean, SerializablePredicate<StrBean>>> {

    protected static class CountingListener implements DataProviderListener {

        private int counter = 0;

        @Override
        public void onDataChange(DataChangeEvent event) {
            ++counter;
        }

        public int getCounter() {
            return counter;
        }
    }

    protected final SerializablePredicate<StrBean> fooFilter = s -> s.getValue()
            .equals("Foo");

    protected final SerializablePredicate<StrBean> gt5Filter = bean -> bean
            .getRandomNumber() > 5;

    protected D dataProvider;
    protected List<StrBean> data = StrBean.generateRandomBeans(100);

    @Before
    public void setUp() {
        dataProvider = createDataProvider();
    }

    protected abstract D createDataProvider();

    protected D getDataProvider() {
        return dataProvider;
    }

    protected abstract void setSortOrder(List<QuerySortOrder> sortOrder,
            Comparator<StrBean> comp);

    private Query<StrBean, SerializablePredicate<StrBean>> createQuery(
            List<QuerySortOrder> sortOrder, Comparator<StrBean> comp) {
        return createQuery(sortOrder, comp, null);
    }

    private Query<StrBean, SerializablePredicate<StrBean>> createQuery(
            List<QuerySortOrder> sortOrder, Comparator<StrBean> comp,
            SerializablePredicate<StrBean> filter) {
        return new Query<>(0, Integer.MAX_VALUE, sortOrder, comp, filter);
    }

    // Tests start here.

    @Test
    public void testListContainsAllData() {
        List<StrBean> list = new LinkedList<>(data);
        dataProvider.fetch(new Query<>())
                .forEach(str -> assertTrue(
                        "Data provider contained values not in original data",
                        list.remove(str)));
        assertTrue("Not all values from original data were in data provider",
                list.isEmpty());
    }

    @Test
    public void testSortByComparatorListsDiffer() {
        Comparator<StrBean> comp = Comparator.comparing(StrBean::getValue)
                .thenComparing(StrBean::getRandomNumber)
                .thenComparing(StrBean::getId);

        List<StrBean> list = dataProvider
                .fetch(createQuery(QuerySortOrder.asc("value")
                        .thenAsc("randomNumber").thenAsc("id").build(), comp))
                .collect(Collectors.toList());

        // First value in data is { Xyz, 10, 100 } which should be last in list
        Assert.assertNotEquals("First value should not match", data.get(0),
                list.get(0));

        Assert.assertEquals("Sorted data and original data sizes don't match",
                data.size(), list.size());

        data.sort(comp);
        for (int i = 0; i < data.size(); ++i) {
            Assert.assertEquals("Sorting result differed", data.get(i),
                    list.get(i));
        }
    }

    @Test
    public void testDefaultSortWithSpecifiedPostSort() {
        Comparator<StrBean> comp = Comparator.comparing(StrBean::getValue)
                .thenComparing(Comparator.comparing(StrBean::getId).reversed());
        setSortOrder(QuerySortOrder.asc("value").thenDesc("id").build(), comp);

        List<StrBean> list = dataProvider
                .fetch(createQuery(QuerySortOrder.asc("randomNumber").build(),
                        Comparator.comparing(StrBean::getRandomNumber)))
                .collect(Collectors.toList());

        Assert.assertEquals("Sorted data and original data sizes don't match",
                data.size(), list.size());

        for (int i = 1; i < list.size(); ++i) {
            StrBean prev = list.get(i - 1);
            StrBean cur = list.get(i);
            // Test specific sort
            Assert.assertTrue(
                    "Failure: " + prev.getRandomNumber() + " > "
                            + cur.getRandomNumber(),
                    prev.getRandomNumber() <= cur.getRandomNumber());

            if (prev.getRandomNumber() == cur.getRandomNumber()) {
                // Test default sort
                Assert.assertTrue(
                        prev.getValue().compareTo(cur.getValue()) <= 0);
                if (prev.getValue().equals(cur.getValue())) {
                    Assert.assertTrue(prev.getId() > cur.getId());
                }
            }
        }
    }

    @Test
    public void testDefaultSortWithFunction() {
        setSortOrder(QuerySortOrder.asc("value").build(),
                Comparator.comparing(StrBean::getValue));

        List<StrBean> list = dataProvider.fetch(new Query<>())
                .collect(Collectors.toList());

        Assert.assertEquals("Sorted data and original data sizes don't match",
                data.size(), list.size());

        for (int i = 1; i < list.size(); ++i) {
            StrBean prev = list.get(i - 1);
            StrBean cur = list.get(i);

            // Test default sort
            Assert.assertTrue(prev.getValue().compareTo(cur.getValue()) <= 0);
        }
    }

    @Test
    public void filteringListDataProvider_convertFilter() {
        DataProvider<StrBean, String> strFilterDataProvider = dataProvider
                .withConvertedFilter(
                        text -> strBean -> strBean.getValue().contains(text));
        Assert.assertEquals("Only one item should match 'Xyz'", 1,
                strFilterDataProvider.size(new Query<>("Xyz")));
        Assert.assertEquals("No item should match 'Zyx'", 0,
                strFilterDataProvider.size(new Query<>("Zyx")));
        Assert.assertEquals("Unexpected number of matches for 'Foo'", 36,
                strFilterDataProvider.size(new Query<>("Foo")));

        Assert.assertEquals("No items should've been filtered out", data.size(),
                strFilterDataProvider.size(new Query<>()));
    }

    @Test
    public void filteringListDataProvider_configurableFilter() {
        ConfigurableFilterDataProvider<StrBean, Void, SerializablePredicate<StrBean>> configurableFilterDataProvider = getDataProvider()
                .withConfigurableFilter();

        configurableFilterDataProvider
                .setFilter(bean -> bean.getValue().contains("Xyz"));

        assertEquals("Only one item should match 'Xyz'", 1,
                configurableFilterDataProvider.size(new Query<>(null)));

        configurableFilterDataProvider
                .setFilter(bean -> bean.getValue().contains("Zyx"));

        assertEquals("No item should match 'Zyx'", 0,
                configurableFilterDataProvider.size(new Query<>(null)));

        configurableFilterDataProvider
                .setFilter(bean -> bean.getValue().contains("Foo"));

        assertEquals("Unexpected number of matches for 'Foo'", 36,
                configurableFilterDataProvider.size(new Query<>(null)));

        configurableFilterDataProvider
                .setFilter(bean -> bean.getValue() == null);

        assertEquals("No items should've been filtered out", 0,
                configurableFilterDataProvider.size(new Query<>(null)));
    }

    @Test
    public void filteringListDataProvider_defaultFilterType() {
        Assert.assertEquals("Only one item should match 'Xyz'", 1,
                dataProvider.size(new Query<>(
                        strBean -> strBean.getValue().contains("Xyz"))));
        Assert.assertEquals("No item should match 'Zyx'", 0, dataProvider.size(
                new Query<>(strBean -> strBean.getValue().contains("Zyx"))));
        Assert.assertEquals("Unexpected number of matches for 'Foo'", 36,
                dataProvider.size(new Query<>(fooFilter)));
    }

    protected long sizeWithUnfilteredQuery() {
        return dataProvider.fetch(new Query<>()).count();
    }

    protected static <F> void assertSizeWithFilter(int expectedSize,
            DataProvider<?, F> dataProvider, F filterValue) {
        Assert.assertEquals(expectedSize,
                dataProvider.size(new Query<>(filterValue)));
    }

}

package com.github.aiderpmsi.pimsdriver.vaadin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.filter.UnsupportedFilterException;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

/**
 * Apache License
 * Derives from SQLContainer from vaadin
 * 
 * @author delabre
 *
 */
public class PmsiProcessContainer implements Container, Container.Filterable, Container.Indexed, Container.Sortable, Container.ItemSetChangeNotifier{

	/**
	 * Generated SerialId
	 */
	private static final long serialVersionUID = 969266900273093568L;
	
	/**
	 * Logger
	 */
	private static final Logger log = Logger.getLogger(PmsiProcessContainer.class.getName());
	
	/**
	 * Query delegate
	 */
	private QueryDelegate delegate;
	
	/**
	 * Cached elements
	 */
	private final LimitedCache<ORID, Item> cachedElements = new LimitedCache<>(512);
	
	/**
	 * Caches the number of elements in the container
	 */
	private final int cachedCount = 0;
	
	/**
	 * Last update of Cached Count
	 */
	private Date cachedCountDate = null;
	
	/**
	 * Max timeout between two caches
	 */
	private final static Long MAX_MILLIS_CACHECOUNT_VALID = new Long(10000);
	
	/**
	 * Filters
	 */
	private final List<Filter> filters = new ArrayList<>();
	
	/**
	 * Orders
	 */
	private final List<String[]> sorters = new ArrayList<>();
	
	public PmsiProcessContainer(QueryDelegate delegate) {
		// SETS THE DELEGATE
		this.delegate = delegate;
		// UPDATE THE NUMBER OF ELEMENTS IN CONTAINER
		updateCount();
	}
	
	private void updateCount() {
		// IF TIMEOUT IS NOT ELAPSED, DO NOT CHANGE
		if (cachedCountDate != null && (new Date().getTime()) - cachedCountDate.getTime() < MAX_MILLIS_CACHECOUNT_VALID) {
			return;
		}
		
		// USE DELEGATES TO SET FILTERS
		try {
			delegate.setFilters(filters);
		} catch (UnsupportedOperationException e) {
			log.log(Level.FINE, "The query delegate doesn't support filtering", e);
		}
		
		// USE DELEGATES TO SET ORDERS
		try {
			delegate.setOrderBy(sorters);
		} catch (UnsupportedOperationException e) {
			log.log(Level.FINE, "The query delegate doesn't support sorting", e);
		}

		// SETS THE NEW COUNT
		cachedCount = delegate.getCount();
		
		// SETS THE NEW COUNT DATE
		cachedCountDate = new Date();

		log.log(Level.FINER, "Updated row count. New count is: {0}", cachedCount);

	}
	
	// ===== LOAD FROM ORIENTDB =====
	private void fillCache() {
		// UPDATE ELEMENTS COUNT IF NECESSARY
		updateCount();

		cachedItems.clear();
		        
		1262

		        itemIndexes.clear();

		1263

		        try {

		1264

		            try {

		1265

		                delegate.setOrderBy(sorters);

		1266

		            } catch (UnsupportedOperationException e) {

		1267

		                /* The query delegate doesn't support sorting. */

		1268

		                /* No need to do anything. */

		1269

		                getLogger().log(Level.FINE,

		1270

		                        "The query delegate doesn't support sorting", e);

		1271

		            }

		1272

		            delegate.beginTransaction();

		1273

		            int fetchedRows = pageLength * CACHE_RATIO;

		1274

		            rs = delegate.getResults(currentOffset, fetchedRows);

		1275

		            rsmd = rs.getMetaData();

		1276

		            List<String> pKeys = delegate.getPrimaryKeyColumns();

		1277

		            // }

		1278

		            /* Create new items and column properties */

		1279

		            ColumnProperty cp = null;

		1280

		            int rowCount = currentOffset;

		1281

		            if (!delegate.implementationRespectsPagingLimits()) {

		1282

		                rowCount = currentOffset = 0;

		1283

		                setPageLengthInternal(size);

		1284

		            }

		1285

		            while (rs.next()) {

		1286

		                List<ColumnProperty> itemProperties = new ArrayList<ColumnProperty>();

		1287

		                /* Generate row itemId based on primary key(s) */

		1288

		                Object[] itemId = new Object[pKeys.size()];

		1289

		                for (int i = 0; i < pKeys.size(); i++) {

		1290

		                    itemId[i] = rs.getObject(pKeys.get(i));

		1291

		                }

		1292

		                RowId id = null;

		1293

		                if (pKeys.isEmpty()) {

		1294

		                    id = new ReadOnlyRowId(rs.getRow());

		1295

		                } else {

		1296

		                    id = new RowId(itemId);

		1297

		                }

		1298

		                List<String> propertiesToAdd = new ArrayList<String>(

		1299

		                        propertyIds);

		1300

		                if (!removedItems.containsKey(id)) {

		1301

		                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {

		1302

		                        if (!isColumnIdentifierValid(rsmd.getColumnLabel(i))) {

		1303

		                            continue;

		1304

		                        }

		1305

		                        String colName = rsmd.getColumnLabel(i);

		1306

		                        Object value = rs.getObject(i);

		1307

		                        Class<?> type = value != null ? value.getClass()

		1308

		                                : Object.class;

		1309

		                        if (value == null) {

		1310

		                            for (String propName : propertyTypes.keySet()) {

		1311

		                                if (propName.equals(rsmd.getColumnLabel(i))) {

		1312

		                                    type = propertyTypes.get(propName);

		1313

		                                    break;

		1314

		                                }

		1315

		                            }

		1316

		                        }

		1317

		                        /*

		1318

		                         * In case there are more than one column with the same

		1319

		                         * name, add only the first one. This can easily happen

		1320

		                         * if you join many tables where each table has an ID

		1321

		                         * column.

		1322

		                         */

		1323

		                        if (propertiesToAdd.contains(colName)) {

		1324

		1325

		                            cp = new ColumnProperty(colName,

		1326

		                                    propertyReadOnly.get(colName),

		1327

		                                    propertyPersistable.get(colName),

		1328

		                                    propertyNullable.get(colName),

		1329

		                                    propertyPrimaryKey.get(colName), value,

		1330

		                                    type);

		1331

		                            itemProperties.add(cp);

		1332

		                            propertiesToAdd.remove(colName);

		1333

		                        }

		1334

		                    }

		1335

		                    /* Cache item */

		1336

		                    itemIndexes.put(rowCount, id);

		1337

		1338

		                    // if an item with the id is contained in the modified

		1339

		                    // cache, then use this record and add it to the cached

		1340

		                    // items. Otherwise create a new item

		1341

		                    int modifiedIndex = indexInModifiedCache(id);

		1342

		                    if (modifiedIndex != -1) {

		1343

		                        cachedItems.put(id, modifiedItems.get(modifiedIndex));

		1344

		                    } else {

		1345

		                        cachedItems.put(id, new RowItem(this, id,

		1346

		                                itemProperties));

		1347

		                    }

		1348

		1349

		                    rowCount++;

		1350

		                }

		1351

		            }

		1352

		            rs.getStatement().close();

		1353

		            rs.close();

		1354

		            delegate.commit();

		1355

		            getLogger().log(Level.FINER, "Fetched {0} rows starting from {1}",

		1356

		                    new Object[] { fetchedRows, currentOffset });

		1357

		        } catch (SQLException e) {

		1358

		            getLogger().log(Level.WARNING,

		1359

		                    "Failed to fetch rows, rolling back", e);

		1360

		            try {

		1361

		                delegate.rollback();

		1362

		            } catch (SQLException e1) {

		1363

		                getLogger().log(Level.SEVERE, "Failed to roll back", e1);

		1364

		            }

		1365

		            try {

		1366

		                if (rs != null) {

		1367

		                    if (rs.getStatement() != null) {

		1368

		                        rs.getStatement().close();

		1369

		                        rs.close();

		1370

		                    }

		1371

		                }

		1372

		            } catch (SQLException e1) {

		1373

		                getLogger().log(Level.WARNING, "Failed to close session", e1);

		1374

		            }

		1375

		            throw new RuntimeException("Failed to fetch page.", e);

		1376

		        }

		1377

		    }
	
	// ===== CONTAINER FUNCTIONS =====
	@Override
	public int indexOfId(Object itemId) {
		// FIRST, FIND IF THIS ITEMID EXISTS
		if (!containsId(itemId)) {
			return -1;
		}
		// IF IT EXISTS, 
		if (cachedElements.isEmpty()) {

		648

		             getPage();

		649

		         }

		650

		         int size = size();

		651

		         // this protects against infinite looping

		652

		         int counter = 0;

		653

		         while (counter < size) {

		654

		             for (Integer i : itemIndexes.keySet()) {

		655

		                 if (itemIndexes.get(i).equals(itemId)) {

		656

		                     return i;

		657

		                 }

		658

		                 counter++;

		659

		             }

		660

		             // load in the next page.

		661

		             int nextIndex = (currentOffset / (pageLength * CACHE_RATIO) + 1)

		662

		                     * (pageLength * CACHE_RATIO);

		663

		             if (nextIndex >= size) {

		664

		                 // Container wrapped around, start from index 0.

		665

		                 nextIndex = 0;

		666

		             }

		667

		             updateOffsetAndCache(nextIndex);

		668

		         }

		669

		         // safeguard in case item not found

		670

		         return -1;
	}

	@Override
	public Item getItem(Object itemId) {
        if (!cachedElements.containsKey(itemId)) {
            int index = indexOfId(itemId);
            if (index >= size) {

281

                // The index is in the added items

282

                int offset = index - size;

283

                RowItem item = addedItems.get(offset);

284

                if (itemPassesFilters(item)) {

285

                    return item;

286

                } else {

287

                    return null;

288

                }

289

            } else {

290

                // load the item into cache

291

                updateOffsetAndCache(index);

292

            }

293

        }

294

        return cachedItems.get(itemId);
	}

	@Override
	public Object nextItemId(Object itemId) {
		return null;
	}

	@Override
	public Object prevItemId(Object itemId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object firstItemId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object lastItemId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFirstId(Object itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLastId(Object itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object addItemAfter(Object previousItemId)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item addItemAfter(Object previousItemId, Object newItemId)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addItemSetChangeListener(ItemSetChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListener(ItemSetChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeItemSetChangeListener(ItemSetChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeListener(ItemSetChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sort(Object[] propertyId, boolean[] ascending) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<?> getSortableContainerPropertyIds() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Object getIdByIndex(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<?> getItemIds(int startIndex, int numberOfItems) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object addItemAt(int index) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item addItemAt(int index, Object newItemId)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addContainerFilter(Filter filter)
			throws UnsupportedFilterException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeContainerFilter(Filter filter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAllContainerFilters() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<Filter> getContainerFilters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<?> getItemIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getType(Object propertyId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean containsId(Object itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Item addItem(Object itemId) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object addItem() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeItem(Object itemId)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addContainerProperty(Object propertyId, Class<?> type,
			Object defaultValue) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeContainerProperty(Object propertyId)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAllItems() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
	}

}

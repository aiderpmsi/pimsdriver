package com.github.aiderpmsi.pimsdriver.db.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.dto.NavigationDTO;
import com.github.aiderpmsi.pimsdriver.dto.UploadedPmsiDTO;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.dto.model.navigation.PmsiOverviewEntry;
import com.github.aiderpmsi.pimsdriver.dto.model.navigation.YM;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class NavigationActions {

	public List<UploadedPmsi> getUploadedPmsi(List<Filter> filters, List<OrderBy> orders,
			Integer first, Integer rows) throws ActionException {

		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			UploadedPmsiDTO upd = new UploadedPmsiDTO(con);) {
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					List<UploadedPmsi> ups = upd.readList(filters, orders, first, rows);
					// SELECTION HAS SUCCEDDED
					con.commit();
					return ups;
				} catch (SQLException e) {
					if (e instanceof SQLException && !((SQLException)e).getSQLState().equals("40001")) {
						con.rollback();
						throw (SQLException) e;
					}
				}
			}
		} catch (SQLException e) {
			throw new ActionException(e);
		}
	}
		
	public Integer getUploadedPmsiSize(List<Filter> filters) throws ActionException {

		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			UploadedPmsiDTO upd = new UploadedPmsiDTO(con);) {
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					int size = (int) upd.listSize(filters);
					// SELECTION HAS SUCCEDDED
					con.commit();
					return size;
				} catch (SQLException e) {
					if (e instanceof SQLException && !((SQLException)e).getSQLState().equals("40001")) {
						con.rollback();
						throw (SQLException) e;
					}
				}
			}
		} catch (SQLException e) {
			throw new ActionException(e);
		}
	}
	
	public List<String> getDistinctFinesses(UploadedPmsi.Status status) throws ActionException {

		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			NavigationDTO upd = new NavigationDTO(con);) {
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					List<String> finesses = upd.readFinessList(status);
					// SELECTION HAS SUCCEDDED
					con.commit();
					return finesses;
				} catch (SQLException e) {
					if (e instanceof SQLException && !((SQLException)e).getSQLState().equals("40001")) {
						con.rollback();
						throw (SQLException) e;
					}
				}
			}
		} catch (SQLException e) {
			throw new ActionException(e);
		}
	}

	public List<YM> getYM(UploadedPmsi.Status status, String finess) throws ActionException {

		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			NavigationDTO upd = new NavigationDTO(con);) {
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					List<YM> yms = upd.readYMList(status, finess);
					// SELECTION HAS SUCCEDDED
					con.commit();
					return yms;
				} catch (SQLException e) {
					if (e instanceof SQLException && !((SQLException)e).getSQLState().equals("40001")) {
						con.rollback();
						throw (SQLException) e;
					}
				}
			}
		} catch (SQLException e) {
			throw new ActionException(e);
		}
	}

	public class Overview {
		public List<PmsiOverviewEntry> rsf;
		public List<PmsiOverviewEntry> rss;
	}
	
	public Overview getOverview(UploadedPmsi model) throws ActionException {

		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			NavigationDTO upd = new NavigationDTO(con);) {
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					Overview overview = new Overview();
					if (model.rsfoid != null)
						overview.rsf = upd.readPmsiOverview(model, "rsfheader");
					else
						overview.rsf = null;
					if (model.rssoid != null)
						overview.rss = upd.readPmsiOverview(model, "rssheader");
					else
						overview.rss = null;
					// SELECTION HAS SUCCEDDED
					con.commit();
					return overview;
				} catch (SQLException e) {
					if (e instanceof SQLException && !((SQLException)e).getSQLState().equals("40001")) {
						con.rollback();
						throw (SQLException) e;
					}
				}
			}
		} catch (SQLException e) {
			throw new ActionException(e);
		}
	}

}

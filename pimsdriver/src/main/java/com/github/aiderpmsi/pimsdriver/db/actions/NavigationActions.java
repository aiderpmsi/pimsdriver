package com.github.aiderpmsi.pimsdriver.db.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.dto.NavigationDTO;
import com.github.aiderpmsi.pimsdriver.dto.UploadedPmsiDTO;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfA;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfB;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
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
		
	public List<BaseRsfA> getFactures(List<Filter> filters, List<OrderBy> orders,
			Integer first, Integer rows) throws ActionException {

		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			NavigationDTO nad = new NavigationDTO(con);) {
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					List<BaseRsfA> rsfas = nad.readRsfAList(filters, orders, first, rows);
					// SELECTION HAS SUCCEDDED
					con.commit();
					return rsfas;
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

	public int getFacturesSize(List<Filter> filters) throws ActionException {

		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			NavigationDTO nad = new NavigationDTO(con);) {
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					int size = (int) nad.readRsfASize(filters);
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

	public BaseRsfA GetFacturesSummary (Long pmel_root) throws ActionException {
		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			NavigationDTO nad = new NavigationDTO(con)) {

			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					BaseRsfA rsfa = nad.readRsfASummary(pmel_root);
					// SELECTION HAS SUCCEDDED
					con.commit();
					return rsfa;
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

	public BaseRsfB GetFacturesBSummary (Long pmel_root, Long pmel_position) throws ActionException {
		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			NavigationDTO nad = new NavigationDTO(con)) {

			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					BaseRsfB rsfb = nad.readRsfBSummary(pmel_root, pmel_position);
					// SELECTION HAS SUCCEDDED
					con.commit();
					return rsfb;
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

	public List<BaseRsfB> getFacturesB(List<Filter> filters, List<OrderBy> orders,
			Integer first, Integer rows) throws ActionException {

		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			NavigationDTO nad = new NavigationDTO(con);) {
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					List<BaseRsfB> rsfbs = nad.readRsfBList(filters, orders, first, rows);
					// SELECTION HAS SUCCEDDED
					con.commit();
					return rsfbs;
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

	public int getFacturesBSize(List<Filter> filters) throws ActionException {

		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			NavigationDTO nad = new NavigationDTO(con);) {
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					int size = (int) nad.readRsfBSize(filters);
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

	public List<NavigationDTO.YM> getYM(UploadedPmsi.Status status, String finess) throws ActionException {

		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			NavigationDTO upd = new NavigationDTO(con);) {
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					List<NavigationDTO.YM> yms = upd.readYMList(status, finess);
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
		public List<NavigationDTO.PmsiOverviewEntry> rsf;
		public List<NavigationDTO.PmsiOverviewEntry> rss;
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

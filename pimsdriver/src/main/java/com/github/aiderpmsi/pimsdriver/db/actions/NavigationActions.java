package com.github.aiderpmsi.pimsdriver.db.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.dto.NavigationDTO;
import com.github.aiderpmsi.pimsdriver.dto.UploadedPmsiDTO;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.dto.model.navigation.RsfOverview;
import com.github.aiderpmsi.pimsdriver.dto.model.navigation.RssOverview;
import com.github.aiderpmsi.pimsdriver.dto.model.navigation.YM;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class NavigationActions {

	public List<UploadedPmsi> getUploadedPmsi(List<Filter> filters, List<OrderBy> orders,
			Integer first, Integer rows) throws ActionException {

		Connection con = null;
		
		try {
			con = DataSourceSingleton.getInstance().getConnection();
			UploadedPmsiDTO upd = new UploadedPmsiDTO();
			
			// STAYS NULL IF SELECTION IS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			List<UploadedPmsi> ups = null;
			while (ups == null) {
				try {
					ups = upd.readList(con, filters, orders, first, rows);
					// SELECTION HAS SUCCEDDED
					con.commit();
				} catch (SQLException e) {
					if (e.getSQLState().equals("40001"))
						ups = null;
					else
						throw e;
				}
			}
			
			return ups;
			
		} catch (SQLException e) {
			// ERROR : ROLLBACK
			try {if (con != null) con.rollback();} catch (SQLException e2) {
				e2.addSuppressed(e);
				throw new ActionException(e2);
			}
			throw new ActionException(e);
		} finally {
			try {if (con != null) con.close();} catch (SQLException e) {
				throw new ActionException(e);
			}
		}
	}

	public Integer getUploadedPmsiSize(List<Filter> filters) throws ActionException {

		Connection con = null;
		
		try {
			con = DataSourceSingleton.getInstance().getConnection();
			UploadedPmsiDTO upd = new UploadedPmsiDTO();
			
			// STAYS NULL IF SELECTION IS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			Integer size = null;
			while (size == null) {
				try {
					size = (int) upd.listSize(con, filters);
					// SELECTION HAS SUCCEDDED
					con.commit();
				} catch (SQLException e) {
					if (e.getSQLState().equals("40001"))
						size = null;
					else
						throw e;
				}
			}
			
			return size;
			
		} catch (SQLException e) {
			// ERROR : ROLLBACK
			try {if (con != null) con.rollback();} catch (SQLException e2) {
				e2.addSuppressed(e);
				throw new ActionException(e2);
			}
			throw new ActionException(e);
		} finally {
			try {if (con != null) con.close();} catch (SQLException e) {
				throw new ActionException(e);
			}
		}
	}
	
	public List<String> getDistinctFinesses(UploadedPmsi.Status status) throws ActionException {

		Connection con = null;
		
		try {
			con = DataSourceSingleton.getInstance().getConnection();
			NavigationDTO upd = new NavigationDTO();
			
			// STAYS NULL IF SELECTION IS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			List<String> finesses = null;
			while (finesses == null) {
				try {
					finesses = upd.readFinessList(con, status);
					// SELECTION HAS SUCCEDDED
					con.commit();
				} catch (SQLException e) {
					if (e.getSQLState().equals("40001"))
						finesses = null;
					else
						throw e;
				}
			}
			
			return finesses;
			
		} catch (SQLException e) {
			// ERROR : ROLLBACK
			try {if (con != null) con.rollback();} catch (SQLException e2) {
				e2.addSuppressed(e);
				throw new ActionException(e2);
			}
			throw new ActionException(e);
		} finally {
			try {if (con != null) con.close();} catch (SQLException e) {
				throw new ActionException(e);
			}
		}
	}

	public List<YM> getYM(UploadedPmsi.Status status, String finess) throws ActionException {

		Connection con = null;
		
		try {
			con = DataSourceSingleton.getInstance().getConnection();
			NavigationDTO upd = new NavigationDTO();
			
			// STAYS NULL IF SELECTION IS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			List<YM> yms = null;
			while (yms == null) {
				try {
					yms = upd.readYMList(con, status, finess);
					// SELECTION HAS SUCCEDDED
					con.commit();
				} catch (SQLException e) {
					if (e.getSQLState().equals("40001"))
						yms = null;
					else
						throw e;
				}
			}
			
			return yms;
			
		} catch (SQLException e) {
			// ERROR : ROLLBACK
			try {if (con != null) con.rollback();} catch (SQLException e2) {
				e2.addSuppressed(e);
				throw new ActionException(e2);
			}
			throw new ActionException(e);
		} finally {
			try {if (con != null) con.close();} catch (SQLException e) {
				throw new ActionException(e);
			}
		}
	}

	public class Overview {
		public RsfOverview rsf;
		public RssOverview rss;
	}
	
	public Overview getOverview(UploadedPmsi model) throws ActionException {

		Connection con = null;
		
		try {
			con = DataSourceSingleton.getInstance().getConnection();
			NavigationDTO upd = new NavigationDTO();
			
			// STAYS NULL IF SELECTION IS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			Overview overview = null;
			while (overview == null) {
				try {
					overview = new Overview();
					if (model.getRsfoid() != null)
						overview.rsf = upd.readRsfOverview(con, model);
					if (model.getRssoid() != null)
						overview.rss = upd.readRssOverview(con, model);
					// SELECTION HAS SUCCEDDED
					con.commit();
				} catch (SQLException e) {
					if (e.getSQLState().equals("40001"))
						overview = null;
					else
						throw e;
				}
			}
			
			return overview;
			
		} catch (SQLException e) {
			// ERROR : ROLLBACK
			try {if (con != null) con.rollback();} catch (SQLException e2) {
				e2.addSuppressed(e);
				throw new ActionException(e2);
			}
			throw new ActionException(e);
		} finally {
			try {if (con != null) con.close();} catch (SQLException e) {
				throw new ActionException(e);
			}
		}
	}

}

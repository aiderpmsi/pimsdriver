package com.github.aiderpmsi.pimsdriver.db.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.dto.NavigationDTO;
import com.github.aiderpmsi.pimsdriver.dto.UploadedPmsiDTO;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfA;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfB;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfC;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRssMain;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class NavigationActions extends DbAction {

	public List<UploadedPmsi> getUploadedPmsi(final List<Filter> filters, final List<OrderBy> orders,
			final Integer first, final Integer rows) throws ActionException {

		return execute(new DbExecution<List<UploadedPmsi>>() {
			@Override
			public List<UploadedPmsi> execute(Connection con) throws SQLException {
				try(UploadedPmsiDTO upd = new UploadedPmsiDTO(con)) {
					return upd.readList(filters, orders, first, rows);
				}
			}
		});
		
	}
		
	public List<BaseRsfA> getFactures(final List<Filter> filters, final List<OrderBy> orders,
			final Integer first, final Integer rows) throws ActionException {

		return execute(new DbExecution<List<BaseRsfA>>() {
			@Override
			public List<BaseRsfA> execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return nad.readRsfAList(filters, orders, first, rows);
				}
			}
		});

	}

	public int getFacturesSize(final List<Filter> filters) throws ActionException {

		return execute(new DbExecution<Integer>() {
			@Override
			public Integer execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return (int) nad.readRsfASize(filters);
				}
			}
		});

	}

	public BaseRsfA GetFacturesSummary (final Long pmel_root) throws ActionException {

		return execute(new DbExecution<BaseRsfA>() {
			@Override
			public BaseRsfA execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return nad.readRsfASummary(pmel_root);
				}
			}
		});

	}

	public BaseRsfB GetFacturesBSummary (final Long pmel_root, final Long pmel_position) throws ActionException {

		return execute(new DbExecution<BaseRsfB>() {
			@Override
			public BaseRsfB execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return nad.readRsfBSummary(pmel_root, pmel_position);
				}
			}
		});

	}

	public BaseRsfC GetFacturesCSummary (final Long pmel_root, final Long pmel_position) throws ActionException {
		
		return execute(new DbExecution<BaseRsfC>() {
			@Override
			public BaseRsfC execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return nad.readRsfCSummary(pmel_root, pmel_position);
				}
			}
		});

	}

	public List<BaseRsfB> getFacturesB(final List<Filter> filters, final List<OrderBy> orders,
			final Integer first, final Integer rows) throws ActionException {

		return execute(new DbExecution<List<BaseRsfB>>() {
			@Override
			public List<BaseRsfB> execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return nad.readRsfBList(filters, orders, first, rows);
				}
			}
		});

	}

	public int getFacturesBSize(final List<Filter> filters) throws ActionException {

		return execute(new DbExecution<Integer>() {
			@Override
			public Integer execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return (int) nad.readRsfBSize(filters);
				}
			}
		});

	}

	public List<BaseRsfC> getFacturesC(final List<Filter> filters, final List<OrderBy> orders,
			final Integer first, final Integer rows) throws ActionException {

		return execute(new DbExecution<List<BaseRsfC>>() {
			@Override
			public List<BaseRsfC> execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return nad.readRsfCList(filters, orders, first, rows);
				}
			}
		});

	}

	public int getFacturesCSize(final List<Filter> filters) throws ActionException {

		return execute(new DbExecution<Integer>() {
			@Override
			public Integer execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return (int) nad.readRsfCSize(filters);
				}
			}
		});

	}

	public List<BaseRssMain> getRssMainList(final List<Filter> filters, final List<OrderBy> orders,
			final Integer first, final Integer rows) throws ActionException {

		return execute(new DbExecution<List<BaseRssMain>>() {
			@Override
			public List<BaseRssMain> execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return nad.readRssMainList(filters, orders, first, rows);
				}
			}
		});

	}

	public int getRssMainSize(final List<Filter> filters) throws ActionException {

		return execute(new DbExecution<Integer>() {
			@Override
			public Integer execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return (int) nad.readRssMainSize(filters);
				}
			}
		});

	}

	public Integer getUploadedPmsiSize(final List<Filter> filters) throws ActionException {

		return execute(new DbExecution<Integer>() {
			@Override
			public Integer execute(Connection con) throws SQLException {
				try(UploadedPmsiDTO upd = new UploadedPmsiDTO(con)) {
					return (int) upd.listSize(filters);
				}
			}
		});

	}
	
	public List<String> getDistinctFinesses(final UploadedPmsi.Status status) throws ActionException {

		return execute(new DbExecution<List<String>>() {
			@Override
			public List<String> execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return nad.readFinessList(status);
				}
			}
		});

	}

	public List<NavigationDTO.YM> getYM(final UploadedPmsi.Status status, final String finess) throws ActionException {

		return execute(new DbExecution<List<NavigationDTO.YM>>() {
			@Override
			public List<NavigationDTO.YM> execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return nad.readYMList(status, finess);
				}
			}
		});

	}

	public class Overview {
		public List<NavigationDTO.PmsiOverviewEntry> rsf;
		public List<NavigationDTO.PmsiOverviewEntry> rss;
	}
	
	public Overview getOverview(final UploadedPmsi model) throws ActionException {

		return execute(new DbExecution<Overview>() {
			@Override
			public Overview execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					Overview overview = new Overview();
					
					overview.rsf = (model.rsfoid == null ? null :
						nad.readPmsiOverview(model, "rsfheader"));

					overview.rss = (model.rssoid == null ? null :
						nad.readPmsiOverview(model, "rssheader"));

					return overview;
				}
			}
		});

	}
	
	public String getPmsiSource(final long pmel_root, final long pmel_position) throws ActionException {

		return execute(new DbExecution<String>() {
			@Override
			public String execute(Connection con) throws SQLException {
				try(NavigationDTO nad = new NavigationDTO(con)) {
					return nad.pmsiSource(pmel_root, pmel_position);
					
				}
			}
		});

	}

}

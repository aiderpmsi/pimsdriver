package com.github.aiderpmsi.pimsdriver.db.actions;

import java.util.List;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.dto.NavigationDTO;
import com.github.aiderpmsi.pimsdriver.dto.UploadedPmsiDTO;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfA;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfB;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfC;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRssActe;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRssDa;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRssDad;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRssMain;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class NavigationActions extends DbAction {

	public NavigationActions(final ServletContext context) {
		super(context);
	}

	public List<UploadedPmsi> getUploadedPmsi(final List<Filter> filters, final List<OrderBy> orders,
			final Integer first, final Integer rows) throws ActionException {

		return execute(UploadedPmsiDTO.class,
				(dto) -> dto.readList(filters, orders, first, rows));
	
	}
		
	public List<BaseRsfA> getFactures(final List<Filter> filters, final List<OrderBy> orders,
			final Integer first, final Integer rows) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> dto.readRsfAList(filters, orders, first, rows));

	}

	public int getFacturesSize(final List<Filter> filters) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> (int) dto.readRsfASize(filters));

	}

	public BaseRsfA GetFacturesSummary (final Long pmel_root) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> dto.readRsfASummary(pmel_root));
		
	}

	public BaseRsfB GetFacturesBSummary (final Long pmel_root, final Long pmel_position) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> dto.readRsfBSummary(pmel_root, pmel_position));

	}

	public BaseRsfC GetFacturesCSummary (final Long pmel_root, final Long pmel_position) throws ActionException {
		
		return execute(NavigationDTO.class,
				(dto) -> dto.readRsfCSummary(pmel_root, pmel_position));

	}

	public List<BaseRsfB> getFacturesB(final List<Filter> filters, final List<OrderBy> orders,
			final Integer first, final Integer rows) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> dto.readRsfBList(filters, orders, first, rows));

	}

	public int getFacturesBSize(final List<Filter> filters) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> (int) dto.readRsfBSize(filters));

	}

	public List<BaseRsfC> getFacturesC(final List<Filter> filters, final List<OrderBy> orders,
			final Integer first, final Integer rows) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> dto.readRsfCList(filters, orders, first, rows));

	}

	public int getFacturesCSize(final List<Filter> filters) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> (int) dto.readRsfCSize(filters));

	}

	public List<BaseRssMain> getRssMainList(final List<Filter> filters, final List<OrderBy> orders,
			final Integer first, final Integer rows) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> dto.readRssMainList(filters, orders, first, rows));

	}

	public int getRssMainSize(final List<Filter> filters) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> (int) dto.readRssMainSize(filters));

	}

	public List<BaseRssDa> getRssDaList(final List<Filter> filters, final List<OrderBy> orders,
			final Integer first, final Integer rows) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> dto.readRssDaList(filters, orders, first, rows));

	}

	public int getRssDaSize(final List<Filter> filters) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> (int) dto.readRssDaSize(filters));

	}

	public List<BaseRssDad> getRssDadList(final List<Filter> filters, final List<OrderBy> orders,
			final Integer first, final Integer rows) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> dto.readRssDadList(filters, orders, first, rows));

	}

	public int getRssDadSize(final List<Filter> filters) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> (int) dto.readRssDadSize(filters));

	}

	public List<BaseRssActe> getRssActeList(final List<Filter> filters, final List<OrderBy> orders,
			final Integer first, final Integer rows) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> dto.readRssActeList(filters, orders, first, rows));

	}

	public int getRssActeSize(final List<Filter> filters) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> (int) dto.readRssActeSize(filters));

	}

	public Integer getUploadedPmsiSize(final List<Filter> filters) throws ActionException {

		return execute(UploadedPmsiDTO.class,
				(dto) -> (int) dto.listSize(filters));

	}
	
	public List<String> getDistinctFinesses(final UploadedPmsi.Status status) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> dto.readFinessList(status));

	}

	public List<NavigationDTO.YM> getYM(final UploadedPmsi.Status status, final String finess) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> dto.readYMList(status, finess));

	}

	public class Overview {
		public List<NavigationDTO.PmsiOverviewEntry> rsf;
		public List<NavigationDTO.PmsiOverviewEntry> rss;
	}
	
	public Overview getOverview(final UploadedPmsi model) throws ActionException {
		
		final Overview overview = new Overview();
		if (model.rsfoid != null) {
			overview.rsf = execute(NavigationDTO.class,
				(dto) -> dto.readPmsiOverview(model, "rsfheader"));
		}
		if (model.rssoid != null) {
			overview.rss = execute(NavigationDTO.class,
					(dto) -> dto.readPmsiOverview(model, "rssheader"));
		}
		return overview;

	}
	
	public String getPmsiSource(final long pmel_root, final long pmel_position) throws ActionException {

		return execute(NavigationDTO.class,
				(dto) -> dto.pmsiSource(pmel_root, pmel_position));

	}

}
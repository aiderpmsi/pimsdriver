package com.github.aiderpmsi.pimsdriver.db.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.dto.CleanupDTO;
import com.vaadin.server.VaadinRequest;

public class CleanupActions extends DbAction {
	
	public CleanupActions(final VaadinRequest request) {
		super(request);
	}

	public List<Long> getToCleanup() throws ActionException {

		try (
				final Connection con = DataSourceSingleton.getConnection(getRequest());
				final CleanupDTO cu = new CleanupDTO(con);) {

			return execute( (connection) -> cu.readList());

		} catch (final SQLException e) {
			throw new ActionException(e);
		}
	}

	public Boolean cleanup(final Long cleanupId) throws ActionException {

		try (
				final Connection con = DataSourceSingleton.getConnection(getRequest());
				final CleanupDTO cu = new CleanupDTO(con);) {
			
			return execute( (connection) -> cu.delete(cleanupId));

		} catch (final SQLException e) {
			throw new ActionException(e);
		}
	}
	
}

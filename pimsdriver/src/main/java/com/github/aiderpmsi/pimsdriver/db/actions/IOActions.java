package com.github.aiderpmsi.pimsdriver.db.actions;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.dto.UploadPmsiDTO;
import com.github.aiderpmsi.pimsdriver.dto.UploadedPmsiDTO;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadPmsi;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;

public class IOActions {

	public void uploadPmsi(UploadPmsi model, InputStream rsf, InputStream rss) throws ActionException {
		
		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			UploadPmsiDTO upd = new UploadPmsiDTO(con);) {
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					upd.create(model, rsf, rss);
					// SELECTION HAS SUCCEDDED
					con.commit();
					return;
				} catch (SQLException | IOException e) {
					if (e instanceof SQLException && !((SQLException)e).getSQLState().equals("40001")) {
						con.rollback();
						throw (SQLException) e;
					} else {
						throw new ActionException(e);
					}
				}
			}
		} catch (SQLException e) {
			throw new ActionException(e);
		}
	}

	public void deletePmsi(UploadedPmsi model) throws ActionException {

		try (Connection con = DataSourceSingleton.getInstance().getConnection();
			UploadedPmsiDTO upd = new UploadedPmsiDTO(con);) {
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					upd.delete(model);
					// DELETION HAS SUCCEDDED
					con.commit();
					return;
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

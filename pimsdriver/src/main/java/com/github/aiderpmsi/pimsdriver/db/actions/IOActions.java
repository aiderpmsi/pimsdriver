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
		Connection con = null;
		
		try {
			con = DataSourceSingleton.getInstance().getConnection();
			UploadPmsiDTO upd = new UploadPmsiDTO();
			
			// STAYS TRUE IF INSERTION IS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			Boolean pending = true;
			while (pending) {
				try {
					upd.create(con, model, rsf, rss);
					// INSERTION HAS SUCCEDDED
					con.commit();
					pending = false;
				} catch (SQLException e) {
					if (e.getSQLState().equals("40001"))
						pending = true;
					else
						throw e;
				}
			}
			
		} catch (SQLException | IOException e) {
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

	public void deletePmsi(UploadedPmsi model) throws ActionException {
		Connection con = null;
		
		try {
			con = DataSourceSingleton.getInstance().getConnection();
			UploadedPmsiDTO upd = new UploadedPmsiDTO();
			
			// STAYS TRUE IF INSERTION IS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			Boolean pending = true;
			while (pending) {
				try {
					upd.delete(con, model);
					// INSERTION HAS SUCCEDDED
					con.commit();
					pending = false;
				} catch (SQLException e) {
					if (e.getSQLState().equals("40001"))
						pending = true;
					else
						throw e;
				}
			}
			
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

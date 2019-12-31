package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import db.DB;
import db.DbException;
import model.dao.DepartmentDao;
import model.entities.Department;

public class DepartmentDaoJDBC implements DepartmentDao {

	private Connection conn;

	public DepartmentDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Department department) {

		PreparedStatement ps = null;

		try {

			ps = conn.prepareStatement("INSERT INTO department(Name) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, department.getName());

			int rowsAffected = ps.executeUpdate();

			if (rowsAffected > 0) {
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next()) {
					department.setId(rs.getInt(1));
				}
				DB.closeResultSet(rs);
			}

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(ps);
		}

	}

	@Override
	public void update(Department department) {

		PreparedStatement ps = null;

		try {

			ps = conn.prepareStatement("UPDATE department SET Name = ? WHERE Id = ?;");
			ps.setString(1, department.getName());
			ps.setInt(2, department.getId());

			ps.executeUpdate();

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(ps);
		}

	}

	@Override
	public void deleteById(Integer id) {

		PreparedStatement ps = null;

		try {

			ps = conn.prepareStatement("DELETE FROM department WHERE Id = ?;");
			ps.setInt(1, id);

			ps.executeUpdate();

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(ps);
		}
		
	}

	@Override
	public Department findById(Integer id) {

		PreparedStatement ps = null;

		try {

			ps = conn.prepareStatement("SELECT * FROM department WHERE Id = ?");
			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();
			Department dep = null;

			if (rs.next()) {

				dep = new Department();
				dep.setId(rs.getInt("Id"));
				dep.setName(rs.getString("Name"));

				DB.closeResultSet(rs);

				return dep;
			}

			return dep;

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(ps);
		}

	}

	@Override
	public List<Department> findAll() {

		PreparedStatement ps = null;

		try {

			ps = conn.prepareStatement("SELECT * FROM department");

			ResultSet rs = ps.executeQuery();
			List<Department> depList = new ArrayList<>();
			Department dep = null;

			while (rs.next()) {
				dep = new Department();
				dep.setId(rs.getInt("Id"));
				dep.setName(rs.getString("Name"));

				depList.add(dep);

			}

			DB.closeResultSet(rs);
			return depList;

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(ps);
		}

	}

}

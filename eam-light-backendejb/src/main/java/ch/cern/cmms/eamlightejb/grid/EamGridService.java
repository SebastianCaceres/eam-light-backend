package ch.cern.cmms.eamlightejb.grid;

import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.client.InforContext;
import ch.cern.eam.wshub.core.services.grids.entities.*;
import ch.cern.eam.wshub.core.tools.InforException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

@Service
public class EamGridService {

    @Autowired
    private InforClient inforClient;

    @Autowired
    private DataSource dataSource;

    public GridRequestResult executeQuery(InforContext context, GridRequest gridRequest) throws InforException {
        String gridId = gridRequest.getGridID();

        if ("WSJOBS".equals(gridId) || "SSPART".equals(gridId) || (gridId != null && gridId.startsWith("OSOBJ"))) {
            try {
                return executeNatively(context, gridId, gridRequest);
            } catch (Exception e) {
                System.out.println("Native grid query failed, falling back to SOAP for grid " + gridId + ": " + e.getMessage());
            }
        }

        return inforClient.getGridsService().executeQuery(context, gridRequest);
    }

    public GridMetadataRequestResult getGridMetadata(InforContext context, String gridID, String type, String lang) throws InforException {
        return inforClient.getGridsService().getGridMetadata(context, gridID, type, lang);
    }

    public GridDDSpyFieldsResult getDDspyFields(InforContext context, String gridID, String type, String dataspyID, String lang) throws InforException {
        return inforClient.getGridsService().getDDspyFields(context, gridID, type, dataspyID, lang);
    }

    public String getGridCsvData(InforContext context, GridRequest gridRequest) throws InforException {
        return inforClient.getGridsService().getGridCsvData(context, gridRequest);
    }

    private GridRequestResult executeNatively(InforContext context, String gridId, GridRequest gridRequest) throws Exception {
        String baseSql;
        if ("WSJOBS".equals(gridId)) {
            baseSql = "SELECT EVT_CODE as workordernum, EVT_DESC as description, EVT_MRC as department, EVT_ORG as organization FROM R5EVENTS";
        } else if ("SSPART".equals(gridId)) {
            baseSql = "SELECT PAR_CODE as partcode, PAR_DESC as description, PAR_ORG as organization FROM R5PARTS";
        } else {
            baseSql = "SELECT OBJ_CODE as equipmentno, OBJ_DESC as equipmentdesc, OBJ_MRC as department, OBJ_ALIAS as alias, OBJ_SERIALNO as serialnumber, OBJ_ORG as organization, OBJ_CLASS as class FROM R5OBJECTS";
        }

        StringBuilder where = new StringBuilder(" WHERE ");
        if ("WSJOBS".equals(gridId)) {
            where.append("EVT_TYPE = 'JOBS'");
        } else if ("OSOBJA".equals(gridId)) {
            where.append("OBJ_OBTYPE = 'A'");
        } else if ("OSOBJP".equals(gridId)) {
            where.append("OBJ_OBTYPE = 'P'");
        } else if ("OSOBJS".equals(gridId)) {
            where.append("OBJ_OBTYPE = 'S'");
        } else if ("OSOBJL".equals(gridId) || "OSOBJL_EQ".equals(gridId) || "LVOBJL_EQ".equals(gridId)) {
            where.append("OBJ_OBTYPE = 'L'");
        } else {
            where.append("1=1");
        }

        List<Object> params = new ArrayList<>();

        // Enforce organization row-level security
        String username = context.getCredentials() != null ? context.getCredentials().getUsername() : null;
        if (username != null) {
            boolean hasWildcardOrg = false;
            String checkSql = "SELECT COUNT(*) FROM R5USERORGANIZATIONS WHERE UPPER(UOR_USER) = UPPER(?) AND UOR_ORG = '*'";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        hasWildcardOrg = true;
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to check wildcard org rights for " + username + ": " + e.getMessage());
            }

            if (!hasWildcardOrg) {
                String orgColumn;
                if ("WSJOBS".equals(gridId)) {
                    orgColumn = "EVT_ORG";
                } else if ("SSPART".equals(gridId)) {
                    orgColumn = "PAR_ORG";
                } else {
                    orgColumn = "OBJ_ORG";
                }
                where.append(" AND ").append(orgColumn).append(" IN (SELECT UOR_ORG FROM R5USERORGANIZATIONS WHERE UPPER(UOR_USER) = UPPER(?))");
                params.add(username);
            }
        }

        List<GridRequestFilter> filters = gridRequest.getGridRequestFilters();
        if (filters != null && !filters.isEmpty()) {
            where.append(" AND (");
            for (int i = 0; i < filters.size(); i++) {
                GridRequestFilter filter = filters.get(i);
                if (i > 0) {
                    where.append(" ").append(filter.getJoiner() != null ? filter.getJoiner().name() : "AND").append(" ");
                }
                if (filter.getLeftParenthesis() != null && filter.getLeftParenthesis()) {
                    where.append("(");
                }

                String dbColumn = mapGridFieldToDbColumn(gridId, filter.getFieldName());
                where.append(getSqlCondition(dbColumn, filter.getOperator(), filter.getFieldValue(), params));

                if (filter.getRightParenthesis() != null && filter.getRightParenthesis()) {
                    where.append(")");
                }
            }
            where.append(")");
        }

        String fullSql = baseSql + where.toString();
        int rowCount = gridRequest.getRowCount() != null ? gridRequest.getRowCount() : 10;

        // Oracle-compatible rownum paging wrapper
        String finalSql = "SELECT * FROM (" + fullSql + ") WHERE ROWNUM <= ?";
        params.add(rowCount);

        List<GridRequestRow> rows = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(finalSql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    GridRequestRow row = new GridRequestRow();
                    List<GridRequestCell> cells = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        String colName = meta.getColumnLabel(i).toLowerCase();
                        if ("rownum".equalsIgnoreCase(colName)) {
                            continue;
                        }
                        GridRequestCell cell = new GridRequestCell(colName, rs.getString(i));
                        cells.add(cell);
                    }
                    row.setCells(cells.toArray(new GridRequestCell[0]));
                    rows.add(row);
                }
            }
        }

        GridRequestResult result = new GridRequestResult();
        result.setRows(rows.toArray(new GridRequestRow[0]));
        return result;
    }

    private String mapGridFieldToDbColumn(String gridId, String fieldName) {
        if ("WSJOBS".equals(gridId)) {
            if ("workordernum".equalsIgnoreCase(fieldName)) return "EVT_CODE";
            if ("description".equalsIgnoreCase(fieldName)) return "EVT_DESC";
            if ("department".equalsIgnoreCase(fieldName)) return "EVT_MRC";
            if ("organization".equalsIgnoreCase(fieldName)) return "EVT_ORG";
        } else if ("SSPART".equals(gridId)) {
            if ("partcode".equalsIgnoreCase(fieldName)) return "PAR_CODE";
            if ("description".equalsIgnoreCase(fieldName)) return "PAR_DESC";
            if ("organization".equalsIgnoreCase(fieldName)) return "PAR_ORG";
        } else {
            if ("equipmentno".equalsIgnoreCase(fieldName)) return "OBJ_CODE";
            if ("equipmentdesc".equalsIgnoreCase(fieldName)) return "OBJ_DESC";
            if ("department".equalsIgnoreCase(fieldName)) return "OBJ_MRC";
            if ("alias".equalsIgnoreCase(fieldName)) return "OBJ_ALIAS";
            if ("serialnumber".equalsIgnoreCase(fieldName)) return "OBJ_SERIALNO";
            if ("organization".equalsIgnoreCase(fieldName)) return "OBJ_ORG";
            if ("class".equalsIgnoreCase(fieldName)) return "OBJ_CLASS";
        }
        return fieldName;
    }

    private String getSqlCondition(String column, String operator, String value, List<Object> params) {
        if ("BEGINS".equalsIgnoreCase(operator)) {
            params.add(value + "%");
            return "UPPER(" + column + ") LIKE UPPER(?)";
        } else if ("EQUALS".equalsIgnoreCase(operator)) {
            params.add(value);
            return "UPPER(" + column + ") = UPPER(?)";
        } else if ("CONTAINS".equalsIgnoreCase(operator)) {
            params.add("%" + value + "%");
            return "UPPER(" + column + ") LIKE UPPER(?)";
        } else if ("IN".equalsIgnoreCase(operator)) {
            String[] values = value.split(",");
            StringBuilder sb = new StringBuilder("UPPER(").append(column).append(") IN (");
            for (int i = 0; i < values.length; i++) {
                sb.append(i > 0 ? ",?" : "?");
                params.add(values[i].trim().toUpperCase());
            }
            sb.append(")");
            return sb.toString();
        }
        params.add(value);
        return "UPPER(" + column + ") = UPPER(?)";
    }
}

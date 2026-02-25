package com.wikex.wikex.util;

import cn.afterturn.easypoi.exception.excel.ExcelExportException;
import cn.afterturn.easypoi.exception.excel.ExcelImportException;
import com.wikex.wikex.annotation.Excel;
import com.wikex.wikex.annotation.ExcelSheet;
import com.wikex.wikex.vo.OtcOrderVO;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelUtil {

    public static <T> void listToExcel(List<T> list, Field[] fields, OutputStream out) throws ExcelExportException {

        if (list.size() == 0 || list == null) {
            throw new ExcelExportException("No data in the data source");
        }
        int sheetSize = 65535;

        WritableWorkbook wwb;
        try {
            wwb = Workbook.createWorkbook(out);

            double sheetNum = Math.ceil(list.size() / new Integer(sheetSize).doubleValue());

            for (int i = 0; i < sheetNum; i++) {

                if (1 == sheetNum) {
                    WritableSheet sheet = wwb.createSheet(OtcOrderVO.class.getAnnotation(ExcelSheet.class).name(), i);
                    fillSheet(sheet, list, fields, 0, list.size() - 1);

                } else {
                    WritableSheet sheet = wwb
                            .createSheet(OtcOrderVO.class.getAnnotation(ExcelSheet.class).name() + (i + 1), i);

                    int firstIndex = i * sheetSize;
                    int lastIndex = (i + 1) * sheetSize - 1 > list.size() - 1 ? list.size() - 1
                            : (i + 1) * sheetSize - 1;

                    fillSheet(sheet, list, fields, firstIndex, lastIndex);
                }
            }

            wwb.write();
            wwb.close();

        } catch (Exception e) {
            e.printStackTrace();

            if (e instanceof ExcelExportException) {
                throw (ExcelExportException) e;

            } else {
                throw new ExcelExportException("Failed to export Excel");
            }
        }

    }

    public static <T> List<T> excelToList(
            InputStream in,
            String sheetName,
            Class<T> entityClass,
            LinkedHashMap<String, String> fieldMap,
            String[] uniqueFields) throws ExcelImportException {

        List<T> resultList = new ArrayList<>();

        try {

            Workbook wb = Workbook.getWorkbook(in);

            Sheet sheet = wb.getSheet(sheetName);

            int realRows = 0;
            for (int i = 0; i < sheet.getRows(); i++) {

                int nullCols = 0;
                for (int j = 0; j < sheet.getColumns(); j++) {
                    Cell currentCell = sheet.getCell(j, i);
                    if (currentCell == null || "".equals(currentCell.getContents().toString())) {
                        nullCols++;
                    }
                }

                if (nullCols == sheet.getColumns()) {
                    break;
                } else {
                    realRows++;
                }
            }

            if (realRows <= 1) {
                throw new ExcelImportException("No data in the Excel file");
            }

            Cell[] firstRow = sheet.getRow(0);

            String[] excelFieldNames = new String[firstRow.length];

            for (int i = 0; i < firstRow.length; i++) {
                excelFieldNames[i] = firstRow[i].getContents().toString().trim();
            }

            boolean isExist = true;
            List<String> excelFieldList = Arrays.asList(excelFieldNames);
            for (String cnName : fieldMap.keySet()) {
                if (!excelFieldList.contains(cnName)) {
                    isExist = false;
                    break;
                }
            }

            if (!isExist) {
                throw new ExcelImportException("Excel is missing required fields, or field names are incorrect");
            }

            LinkedHashMap<String, Integer> colMap = new LinkedHashMap<String, Integer>();
            for (int i = 0; i < excelFieldNames.length; i++) {
                colMap.put(excelFieldNames[i], firstRow[i].getColumn());
            }

            Cell[][] uniqueCells = new Cell[uniqueFields.length][];
            for (int i = 0; i < uniqueFields.length; i++) {
                int col = colMap.get(uniqueFields[i]);
                uniqueCells[i] = sheet.getColumn(col);
            }

            for (int i = 1; i < realRows; i++) {
                int nullCols = 0;
                for (int j = 0; j < uniqueFields.length; j++) {
                    String currentContent = uniqueCells[j][i].getContents();
                    Cell sameCell = sheet.findCell(currentContent,
                            uniqueCells[j][i].getColumn(),
                            uniqueCells[j][i].getRow() + 1,
                            uniqueCells[j][i].getColumn(),
                            uniqueCells[j][realRows - 1].getRow(),
                            true);
                    if (sameCell != null) {
                        nullCols++;
                    }
                }

                if (nullCols == uniqueFields.length) {
                    throw new ExcelImportException("Duplicate rows found in Excel, please check");
                }
            }

            for (int i = 1; i < realRows; i++) {

                T entity = entityClass.newInstance();

                for (Map.Entry<String, String> entry : fieldMap.entrySet()) {

                    String cnNormalName = entry.getKey();

                    String enNormalName = entry.getValue();

                    int col = colMap.get(cnNormalName);

                    String content = sheet.getCell(col, i).getContents().toString().trim();

                    setFieldValueByName(enNormalName, content, entity);
                }

                resultList.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (e instanceof ExcelImportException) {
                throw (ExcelImportException) e;

            } else {
                e.printStackTrace();
                throw new ExcelImportException("Failed to import Excel");
            }
        }
        return resultList;
    }

    private static Object getFieldValueByName(String fieldName, Object o) throws Exception {

        Object value = null;
        Field field = getFieldByName(fieldName, o.getClass());

        if (field != null) {
            field.setAccessible(true);
            value = field.get(o);
        } else {
            throw new ExcelImportException(o.getClass().getSimpleName() + " class does not contain field name " + fieldName);
        }

        return value;
    }

    private static Field getFieldByName(String fieldName, Class<?> clazz) {

        Field[] selfFields = clazz.getDeclaredFields();

        for (Field field : selfFields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }

        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && superClazz != Object.class) {
            return getFieldByName(fieldName, superClazz);
        }

        return null;
    }

    private static Object getFieldValueByNameSequence(String fieldNameSequence, Object o) throws Exception {

        Object value = null;

        String[] attributes = fieldNameSequence.split("\\.");
        if (attributes.length == 1) {
            value = getFieldValueByName(fieldNameSequence, o);
        } else {

            Object fieldObj = getFieldValueByName(attributes[0], o);
            String subFieldNameSequence = fieldNameSequence.substring(fieldNameSequence.indexOf(".") + 1);
            value = getFieldValueByNameSequence(subFieldNameSequence, fieldObj);
        }
        return value;

    }

    private static void setFieldValueByName(String fieldName, Object fieldValue, Object o) throws Exception {

        Field field = getFieldByName(fieldName, o.getClass());
        if (field != null) {
            field.setAccessible(true);

            Class<?> fieldType = field.getType();

            if (String.class == fieldType) {
                field.set(o, String.valueOf(fieldValue));
            } else if ((Integer.TYPE == fieldType)
                    || (Integer.class == fieldType)) {
                field.set(o, Integer.parseInt(fieldValue.toString()));
            } else if ((Long.TYPE == fieldType)
                    || (Long.class == fieldType)) {
                field.set(o, Long.valueOf(fieldValue.toString()));
            } else if ((Float.TYPE == fieldType)
                    || (Float.class == fieldType)) {
                field.set(o, Float.valueOf(fieldValue.toString()));
            } else if ((Short.TYPE == fieldType)
                    || (Short.class == fieldType)) {
                field.set(o, Short.valueOf(fieldValue.toString()));
            } else if ((Double.TYPE == fieldType)
                    || (Double.class == fieldType)) {
                field.set(o, Double.valueOf(fieldValue.toString()));
            } else if (Character.TYPE == fieldType) {
                if ((fieldValue != null) && (fieldValue.toString().length() > 0)) {
                    field.set(o, Character
                            .valueOf(fieldValue.toString().charAt(0)));
                }
            } else if (Date.class == fieldType) {
                field.set(o, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fieldValue.toString()));
            } else {
                field.set(o, fieldValue);
            }
        } else {
            throw new ExcelImportException(o.getClass().getSimpleName() + " class does not contain field name " + fieldName);
        }
    }

    private static void setColumnAutoSize(WritableSheet ws, int extraWith) {

        for (int i = 0; i < ws.getColumns(); i++) {
            int colWith = 0;
            for (int j = 0; j < ws.getRows(); j++) {
                String content = ws.getCell(i, j).getContents().toString();
                int cellWith = content.length();
                if (colWith < cellWith) {
                    colWith = cellWith;
                }
            }

            ws.setColumnView(i, colWith + extraWith);
        }

    }

    private static <T> void fillSheet(
            WritableSheet sheet,
            List<T> list,
            LinkedHashMap<String, String> fieldMap,
            int firstIndex,
            int lastIndex) throws Exception {

        String[] enFields = new String[fieldMap.size()];
        String[] cnFields = new String[fieldMap.size()];

        int count = 0;
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            enFields[count] = entry.getKey();
            cnFields[count] = entry.getValue();
            count++;
        }

        for (int i = 0; i < cnFields.length; i++) {
            Label label = new Label(i, 0, cnFields[i]);
            sheet.addCell(label);
        }

        int rowNo = 1;
        for (int index = firstIndex; index <= lastIndex; index++) {

            T item = list.get(index);
            for (int i = 0; i < enFields.length; i++) {
                Object objValue = getFieldValueByNameSequence(enFields[i], item);
                String fieldValue = objValue == null ? "" : objValue.toString();
                Label label = new Label(i, rowNo, fieldValue);
                sheet.addCell(label);
            }

            rowNo++;
        }

        setColumnAutoSize(sheet, 5);
    }

    private static <T> void fillSheet(
            WritableSheet sheet,
            List<T> list,
            Field[] fields,
            int firstIndex,
            int lastIndex) throws Exception {

        int j = 0;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getAnnotation(Excel.class) == null) {
                continue;
            }
            Label label = new Label(j, 0, fields[i].getAnnotation(Excel.class).name());
            sheet.addCell(label);
            j++;
        }

        int rowNo = 1;
        for (int index = firstIndex; index <= lastIndex; index++) {

            T item = list.get(index);
            int k = 0;
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getAnnotation(Excel.class) == null) {
                    continue;
                }
                Object objValue = getFieldValueByNameSequence(fields[i].getName(), item);
                String fieldValue = objValue == null ? "" : objValue.toString();
                Label label = new Label(k, rowNo, fieldValue);
                sheet.addCell(label);
                k++;
            }

            rowNo++;
        }

        setColumnAutoSize(sheet, 5);
    }

}

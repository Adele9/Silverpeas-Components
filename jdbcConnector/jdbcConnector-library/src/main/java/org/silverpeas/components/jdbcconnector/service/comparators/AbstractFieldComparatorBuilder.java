/*
 * Copyright (C) 2000 - 2018 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.jdbcconnector.service.comparators;

import org.silverpeas.core.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;

/**
 * @author mmoquillon
 */
public abstract class AbstractFieldComparatorBuilder implements FieldComparatorBuilder {

  @SuppressWarnings("unchecked")
  protected <T> T convert(final String value, final Class<T> type) {
    if (type.equals(String.class)) {
      return (T) value;
    }
    try {
      Method valueOf = type.getMethod("valueOf", String.class);
      return (T) valueOf.invoke(type, value);

    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new ClassCastException(e.getMessage());
    }
  }

  @Override
  public String compare(final String fieldName, final String value, final Class<?> type) {
    if (StringUtil.isNotDefined(fieldName)) {
      return "";
    }
    return getFormatter().format(new String[] {fieldName, encode(value, type)});
  }

  protected abstract MessageFormat getFormatter();

  protected String encode(final String value, final Class<?> type) {
    return type.equals(String.class) ? "'" + value + "'" : value;
  }

}
  
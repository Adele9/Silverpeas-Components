package org.silverpeas.components.formsonline.model;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.util.PaginationList;
import org.silverpeas.core.util.SilverpeasArrayList;
import org.silverpeas.core.util.SilverpeasList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.silverpeas.components.formsonline.model.FormInstance.*;

/**
 * @author Nicolas Eysseric
 */
public class RequestsByStatus {

  static final List<Pair<List<Integer>, BiConsumer<RequestsByStatus, SilverpeasList<FormInstance>>>>
      MERGING_RULES_BY_STATES =
      asList(Pair.of(singletonList(STATE_REFUSED), RequestsByStatus::addDenied),
          Pair.of(singletonList(STATE_VALIDATED), RequestsByStatus::addValidated),
          Pair.of(singletonList(STATE_ARCHIVED), RequestsByStatus::addArchived),
          Pair.of(asList(STATE_UNREAD, STATE_READ), RequestsByStatus::addToValidate));

  private static final Comparator<FormInstance> FORM_INSTANCE_COMPARATOR = (a, b) -> {
    int c = b.getCreationDate().compareTo(a.getCreationDate());
    if (c == 0) {
      c = Integer.valueOf(b.getId()) - Integer.valueOf(a.getId());
    }
    return c;
  };
  private final PaginationPage paginationPage;

  private SilverpeasList<FormInstance> toValidateList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> validatedList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> deniedList = new SilverpeasArrayList<>();
  private SilverpeasList<FormInstance> archivedList = new SilverpeasArrayList<>();

  RequestsByStatus(final PaginationPage paginationPage) {
    this.paginationPage = paginationPage;
  }

  private void addArchived(final SilverpeasList<FormInstance> formInstances) {
    archivedList = merge(formInstances, archivedList);
  }

  private void addDenied(final SilverpeasList<FormInstance> formInstances) {
    deniedList = merge(formInstances, deniedList);
  }

  private void addValidated(final SilverpeasList<FormInstance> formInstances) {
    validatedList = merge(formInstances, validatedList);
  }

  private void addToValidate(final SilverpeasList<FormInstance> formInstances) {
    toValidateList = merge(formInstances, toValidateList);
  }

  public SilverpeasList<FormInstance> getToValidate() {
    return toValidateList;
  }

  public SilverpeasList<FormInstance> getDenied() {
    return deniedList;
  }

  public SilverpeasList<FormInstance> getValidated() {
    return validatedList;
  }

  public SilverpeasList<FormInstance> getArchived() {
    return archivedList;
  }

  public boolean isEmpty() {
    return getValidated().isEmpty() && getToValidate().isEmpty() && getDenied().isEmpty() &&
        getArchived().isEmpty();
  }

  public SilverpeasList<FormInstance> getAll() {
    return merge(getToValidate(), getValidated(), getDenied(), getArchived());
  }

  /**
   * Merges the two given lists without modifying them into a new one.
   * @param lists the lists to merge.
   * @return the list which is the result of merge.
   */
  @SafeVarargs
  private final SilverpeasList<FormInstance> merge(final SilverpeasList<FormInstance>... lists) {
    int size = 0;
    int maxSize = 0;
    for (SilverpeasList<FormInstance> list : lists) {
      size += list.size();
      maxSize += list.originalListSize();
    }
    final List<FormInstance> merge = new ArrayList<>(size);
    for (SilverpeasList<FormInstance> list : lists) {
      merge.addAll(list);
    }
    Stream<FormInstance> resultStream = merge.stream().sorted(FORM_INSTANCE_COMPARATOR);
    if (paginationPage != null) {
      resultStream = resultStream.limit(paginationPage.getPageSize());
    }
    return PaginationList.from(resultStream.collect(Collectors.toList()), maxSize);
  }
}
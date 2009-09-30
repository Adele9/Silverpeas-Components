/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.resourcesmanager.servlets;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.resourcesmanager.control.ResourcesManagerSessionController;
import com.silverpeas.resourcesmanager.model.CategoryDetail;
import com.silverpeas.resourcesmanager.model.ReservationDetail;
import com.silverpeas.resourcesmanager.model.ResourceDetail;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendar;

public class ResourcesManagerRequestRouter extends ComponentRequestRouter {
  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "ResourcesManager";
  }

  /**
   * Method declaration
   * 
   * 
   * @param mainSessionCtrl
   * @param componentContext
   * 
   * @return
   * 
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ResourcesManagerSessionController(mainSessionCtrl,
        componentContext);
  }

  private CategoryDetail request2CategoryDetail(HttpServletRequest request) {
    String name = request.getParameter("name");
    String bookable = request.getParameter("bookable");
    String form = request.getParameter("form");
    String responsible = request.getParameter("responsible");
    String description = request.getParameter("description");
    boolean book = (bookable != null && bookable.equals("on"));
    CategoryDetail category = new CategoryDetail(name, book, form, responsible,
        description);
    if (request.getParameter("id") != null) {
      String categoryId = request.getParameter("id");
      category.setId(categoryId);
    }
    return category;
  }

  private ResourceDetail request2ResourceDetail(List items) {
    String name = FileUploadUtil.getParameter(items, "SPRM_name");
    String bookable = FileUploadUtil.getParameter(items, "SPRM_bookable");
    String responsible = FileUploadUtil.getParameter(items, "SPRM_responsible");
    String description = FileUploadUtil.getParameter(items, "SPRM_description");
    String categoryid = FileUploadUtil.getParameter(items,
        "SPRM_categoryChoice");
    boolean book = (bookable != null && bookable.equals("on"));
    ResourceDetail resource = new ResourceDetail(name, categoryid, responsible,
        description, book);
    String resourceId = FileUploadUtil.getParameter(items, "SPRM_resourceId");
    if (StringUtil.isDefined(resourceId))
      resource.setId(resourceId);

    return resource;
  }

  private ReservationDetail request2ReservationDetail(
      HttpServletRequest request, ComponentSessionController resourcesManagerSC) {
    ReservationDetail reservation = null;
    try {
      String evenement = request.getParameter("evenement");
      String startDate = request.getParameter("startDate");
      String startHour = request.getParameter("startHour");
      String endHour = request.getParameter("endHour");
      String endDate = request.getParameter("endDate");
      String raison = request.getParameter("raison");
      String lieu = request.getParameter("lieu");
      Date dateDebut = DateUtil.stringToDate(startDate, startHour,
          resourcesManagerSC.getLanguage());
      Date dateFin = DateUtil.stringToDate(endDate, endHour, resourcesManagerSC
          .getLanguage());
      reservation = new ReservationDetail(evenement, dateDebut, dateFin,
          raison, lieu);
      SilverTrace.info("resourcesManager",
          "ResourcesManagerRequestRouter.request2ReservationDetail()",
          "root.MSG_GEN_PARAM_VALUE", "reservation=" + reservation);
      return reservation;
    } catch (Exception e) {
      // TODO: handle exception
    }
    return reservation;
  }

  /**
   * This method has to be implemented by the component request rooter it has to
   * compute a destination page
   * 
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param componentSC
   *          The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   *         "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String categoryId = "";
    String reservationId;
    String resourceId;
    // String responsibleId;

    String destination = "";
    ResourcesManagerSessionController resourcesManagerSC = (ResourcesManagerSessionController) componentSC;
    request.setAttribute("rsc", resourcesManagerSC);
    String flag = getFlag(resourcesManagerSC.getUserRoles());
    String userId = resourcesManagerSC.getUserId();
    request.setAttribute("Profile", flag);
    request.setAttribute("UserId", userId);
    String root = "/resourcesManager/jsp/";
    try {
      if (function.startsWith("Main")) {
        destination = getDestination("Calendar", componentSC, request);
      }
      /*********** Gestion des cat�gories ******************/
      else if (function.equals("NewCategory")) {
        List listTemplates = PublicationTemplateManager
            .getPublicationTemplates();

        request.setAttribute("listTemplates", listTemplates);

        destination = root + "categoryManager.jsp";
      } else if (function.equals("SaveCategory")) {
        CategoryDetail category = request2CategoryDetail(request);
        resourcesManagerSC.createCategory(category);
        destination = getDestination("ViewCategories", componentSC, request);
      } else if (function.equals("EditCategory")) {
        categoryId = request.getParameter("id");
        CategoryDetail category = resourcesManagerSC.getCategory(categoryId);
        List listTemplates = PublicationTemplateManager
            .getPublicationTemplates();

        request.setAttribute("listTemplates", listTemplates);
        request.setAttribute("category", category);

        destination = root + "categoryManager.jsp";
      } else if (function.equals("ModifyCategory")) {
        CategoryDetail category = request2CategoryDetail(request);
        resourcesManagerSC.updateCategory(category);
        destination = getDestination("ViewCategories", componentSC, request);
      } else if (function.equals("ViewCategories")) {
        List list = resourcesManagerSC.getCategories();
        request.setAttribute("categories", list);
        destination = root + "categories.jsp";
      } else if (function.equals("DeleteCategory")) {
        categoryId = request.getParameter("id");
        resourcesManagerSC.deleteCategory(categoryId);
        destination = getDestination("ViewCategories", componentSC, request);
      }
      /*********** Gestion des ressources ******************/
      else if (function.equals("NewResource")) {
        // categorie pr�-s�l�ctionn�e
        categoryId = request.getParameter("categoryId");
        List list = resourcesManagerSC.getCategories();
        request.setAttribute("listCategories", list);
        request.setAttribute("categoryId", categoryId);

        resourcesManagerSC.setResourceIdForResource(null);
        resourcesManagerSC.setCategoryIdForResource(categoryId);
        setXMLFormIntoRequest(request, resourcesManagerSC);

        destination = root + "resourceManager.jsp";
      } else if (function.equals("SaveResource")) {
        // r�cup�ration des donn�es saisies dans le formulaire
        List items = FileUploadUtil.parseRequest(request);

        ResourceDetail resource = request2ResourceDetail(items);
        String idResource = resourcesManagerSC.createResource(resource);
        request.setAttribute("resourceId", idResource);
        request.setAttribute("provenance", "resources");

        resourcesManagerSC.setResourceIdForResource(idResource);
        updateXMLForm(resourcesManagerSC, items);

        destination = getDestination("ViewResource", componentSC, request);
      } else if (function.equals("EditResource")) {
        resourceId = request.getParameter("resourceId");
        ResourceDetail resource = resourcesManagerSC.getResource(resourceId);

        // on r�cup�re l'ensemble des cat�gories pour la liste d�roulante
        List list = resourcesManagerSC.getCategories();
        request.setAttribute("listCategories", list);

        request.setAttribute("categoryId", resource.getCategoryId());
        request.setAttribute("resource", resource);

        resourcesManagerSC.setResourceIdForResource(resourceId);
        resourcesManagerSC.setCategoryIdForResource(resource.getCategoryId());
        setXMLFormIntoRequest(request, resourcesManagerSC);

        destination = root + "resourceManager.jsp";
      } else if (function.equals("ModifyResource")) {
        // r�cup�ration des donn�es saisies dans le formulaire
        List items = FileUploadUtil.parseRequest(request);

        ResourceDetail resource = request2ResourceDetail(items);
        resourcesManagerSC.updateResource(resource);

        resourcesManagerSC.setResourceIdForResource(resource.getId());
        updateXMLForm(resourcesManagerSC, items);

        request.setAttribute("id", resource.getCategoryId());
        destination = getDestination("ViewResources", componentSC, request);
      } else if (function.equals("ViewResources")) {
        if (request.getAttribute("id") != null) {
          categoryId = (String) request.getAttribute("id");
        } else if (request.getParameter("id") != null) {
          categoryId = (String) request.getParameter("id");
        }
        List list = resourcesManagerSC.getResourcesByCategory(categoryId);
        List listcategories = resourcesManagerSC.getCategories();
        request.setAttribute("listCategories", listcategories);
        request.setAttribute("list", list);
        request.setAttribute("categoryId", categoryId);
        destination = root + "resources.jsp";
      } else if (function.equals("ViewResource")) {
        String provenance = null;
        if (request.getParameter("provenance") != null) {
          provenance = request.getParameter("provenance");
          resourcesManagerSC.setProvenanceResource(provenance);
        } else if (request.getAttribute("provenance") != null) {
          provenance = (String) request.getAttribute("provenance");
          resourcesManagerSC.setProvenanceResource(provenance);
        } else {
          provenance = resourcesManagerSC.getProvenanceResource();
        }
        String idReservation = request.getParameter("reservationId");
        if (idReservation != null) {
          resourcesManagerSC.setReservationIdForResource(idReservation);
        }
        if (request.getParameter("resourceId") != null) {
          resourceId = request.getParameter("resourceId");
          resourcesManagerSC.setResourceIdForResource(resourceId);
        } else if (request.getAttribute("resourceId") != null) {
          resourceId = (String) request.getAttribute("resourceId");
          resourcesManagerSC.setResourceIdForResource(resourceId);
        } else {
          resourceId = resourcesManagerSC.getResourceIdForResource();
        }
        ResourceDetail resource = resourcesManagerSC.getResource(resourceId);
        CategoryDetail category = resourcesManagerSC.getCategory(resource
            .getCategoryId());

        resourcesManagerSC.setCategoryIdForResource(category.getId());

        if (StringUtil.isDefined(category.getForm()))
          putXMLDisplayerIntoRequest(resource, request, resourcesManagerSC);

        request.setAttribute("category", category);
        request.setAttribute("provenance", provenance);
        request.setAttribute("resource", resource);
        request.setAttribute("ShowComments", Boolean.valueOf(resourcesManagerSC
            .areCommentsEnabled()));
        destination = root + "resource.jsp";
      } else if (function.equals("DeleteRessource")) {
        resourceId = request.getParameter("resourceId");
        categoryId = request.getParameter("categoryId");
        resourcesManagerSC.deleteResource(resourceId);
        request.setAttribute("id", categoryId);
        destination = getDestination("ViewResources", componentSC, request);
      }
      /*********** Gestion des reservations ******************/
      else if (function.equals("NewReservation")) {
        String date = request.getParameter("Day");

        if (StringUtil.isDefined(date))
          request.setAttribute("DefaultDate", date);

        destination = root + "reservationManager.jsp";
      } else if (function.equals("GetAvailableResources")) {
        String idReservation = null;
        if (request.getParameter("reservationId") != null) {
          idReservation = (String) request.getParameter("reservationId");
        } else if (request.getAttribute("reservationId") != null) {
          idReservation = (String) request.getAttribute("reservationId");
        }
        List maListResourcesReservable = null;
        ReservationDetail reservation = null;
        List listResourcesProblem = (List) request
            .getAttribute("listeResourcesProblem");
        List listResourceEverReserved = null;
        // si listResourcesProblem c'est qu'il n y a pas eu de probl�me
        // d'enregistrement
        if ((listResourcesProblem == null)) {
          reservation = request2ReservationDetail(request, resourcesManagerSC);
          resourcesManagerSC.createReservation(reservation);
          resourcesManagerSC
              .setBeginDateReservation(reservation.getBeginDate());
          resourcesManagerSC.setEndDateReservation(reservation.getEndDate());
        }
        if (idReservation != null) {
          SilverTrace.info("resourcesManager",
              "ResourcesManagerRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "dans le if,idReservation="
                  + idReservation);
          listResourceEverReserved = resourcesManagerSC
              .getResourcesofReservation(idReservation);
          if (listResourceEverReserved != null) {
            // boucle permettant de supprimer les r�servations d�j� r�serv�es
            // qui posent probl�me, car elles ont d�j� �t� r�serv�es
            for (int i = 0; i < listResourceEverReserved.size(); i++) {
              ResourceDetail resourceReserved = (ResourceDetail) listResourceEverReserved
                  .get(i);
              if (listResourcesProblem != null) {
                for (int j = 0; j < listResourcesProblem.size(); j++) {
                  ResourceDetail resourceProblem = (ResourceDetail) listResourcesProblem
                      .get(j);
                  if (resourceReserved.equals(resourceProblem)) {
                    listResourceEverReserved.remove(i);
                    break;
                  }
                }
              }
            }
          }
        }
        int nbCategories = resourcesManagerSC.getCategories().size();
        reservation = resourcesManagerSC.getReservationCourante();
        maListResourcesReservable = resourcesManagerSC.getResourcesReservable(
            reservation.getBeginDate(), reservation.getEndDate());
        SilverTrace.info("resourcesManager",
            "ResourcesManagerRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "listResourcesReservable="
                + maListResourcesReservable.size());
        // on envoie l'id de la r�servation et l'ensemble des resources
        // associ�es � celles -ci
        request.setAttribute("idReservation", idReservation);
        request.setAttribute("listResourceEverReserved",
            listResourceEverReserved);

        request.setAttribute("listResourcesReservable",
            maListResourcesReservable);
        request.setAttribute("reservation", reservation);
        request.setAttribute("listResourcesProblem", listResourcesProblem);
        request.setAttribute("nbCategories", new Integer(nbCategories));
        destination = root + "cart.jsp";
      } else if (function.equals("FinalReservation")) {
        // listeReservation est la liste compl�te des ressources r�serv�es lors
        // d'une cr�ation ou d'une �dition de r�servation
        // idModifiedReservation est l'id de la r�servation � modifi� lors d'une
        // �dition de r�servation
        // on doit v�rifier que celles-ci n'ont pas �t� prises avant de mettre �
        // jour la r�servation.
        String idModifiedReservation = request
            .getParameter("idModifiedReservation");
        String listeReservation = request.getParameter("listeResa");
        // si idModifiedReservation n'est pas nulle, on est en train de modifier
        // une r�servation
        // sinon on est en train de cr�er une r�servation
        if (idModifiedReservation != null) {
          // on v�rifie que les nouvelles ressources que l'on veut r�server ne
          // sont pas d�j� prises.
          List listeResourcesProblemeReservationTotal = resourcesManagerSC
              .getResourcesProblemDate(listeReservation, resourcesManagerSC
                  .getBeginDateReservation(), resourcesManagerSC
                  .getEndDateReservation(), idModifiedReservation);
          if (listeResourcesProblemeReservationTotal.isEmpty()) {
            resourcesManagerSC.updateReservation(idModifiedReservation,
                listeReservation);
            destination = getDestination("Calendar", componentSC, request);
          } else {
            request.setAttribute("listeResourcesProblem",
                listeResourcesProblemeReservationTotal);
            request.setAttribute("reservationId", idModifiedReservation);
            destination = getDestination("GetAvailableResources", componentSC,
                request);
          }
        } else {
          resourcesManagerSC.setListReservationCurrent(listeReservation);
          List listeResourcesProblem = resourcesManagerSC
              .verificationReservation(listeReservation);
          if (listeResourcesProblem.isEmpty()) {
            resourcesManagerSC.saveReservation();
            destination = getDestination("Calendar", componentSC, request);
          } else {
            request
                .setAttribute("listeResourcesProblem", listeResourcesProblem);
            destination = getDestination("GetAvailableResources", componentSC,
                request);
          }
        }
      } else if (function.equals("EditReservation")) {
        String idReservation = "";
        // listResources est la liste des ressources qui peuvent poser probl�me
        // quand on change la date de r�servation
        List listResourcesProblem = null;
        if (request.getParameter("id") != null) {
          idReservation = request.getParameter("id");
        } else if (request.getAttribute("id") != null) {
          idReservation = (String) request.getAttribute("id");
          listResourcesProblem = (List) request.getAttribute("listResources");
        }
        ReservationDetail reservation = resourcesManagerSC
            .getReservation(idReservation);
        // on envoie la r�servation de l'id et la liste des ressources associ�es
        // ainsi que la liste qui posent probl�me quand on change les dates
        request.setAttribute("reservation", reservation);
        // request.setAttribute("listResourceEverReserved",
        // listResourceEverReserved);
        request.setAttribute("listResourcesProblem", listResourcesProblem);
        destination = root + "reservationManager.jsp";
      } else if (function.equals("ViewReservation")) {
        reservationId = null;
        if (request.getParameter("reservationId") != null)
          reservationId = request.getParameter("reservationId");
        else if (request.getAttribute("reservationId") != null)
          reservationId = (String) request.getAttribute("reservationId");
        // si on vient de resource.jsp, reservationId a �t� stock� dans le
        // session controler
        if (reservationId == null) {
          reservationId = resourcesManagerSC.getReservationIdForResource();
        }
        ReservationDetail reservation = resourcesManagerSC
            .getReservation(reservationId);
        List listResourcesofReservation = resourcesManagerSC
            .getResourcesofReservation(reservationId);
        request.setAttribute("listResourcesofReservation",
            listResourcesofReservation);
        request.setAttribute("reservationId", reservationId);
        request.setAttribute("reservation", reservation);
        destination = root + "viewReservation.jsp";
      } else if (function.equals("ViewReservations")) {
        List listOfReservation = resourcesManagerSC.getReservationUser();
        request.setAttribute("listOfReservation", listOfReservation);
        destination = root + "viewReservations.jsp";
      } else if (function.equals("DeleteReservation")) {
        resourceId = request.getParameter("id");
        resourcesManagerSC.deleteReservation(resourceId);
        destination = getDestination("Calendar", componentSC, request);
        // destination = root + "almanach.jsp";
      } else if (function.equals("Calendar")) {
        /***
         * on regarde soit : ses r�servations -> myObjectView = myReservation
         * les r�servations d'une autre personne -> myObjectView =
         * PlanningOtherUser le planning d'une cat�gorie -> myObjectView = l'id
         * de la cat�gorie le planning d'une ressource -> myObjectView = l'id de
         * la cat�gorie myObjectView repr�sente ce qu'on est en train de
         * visualiser, il est nul � l'initialisation.
         * 
         * idUser sert � savoir l'id de la personne dont on regarde le
         * calandrier
         */
        String myObjectView = null;
        if (request.getParameter("objectView") != null)
          myObjectView = request.getParameter("objectView");
        else if (request.getAttribute("objectView") != null)
          myObjectView = (String) request.getAttribute("objectView");

        String idUser = (String) request.getAttribute("userId");
        // on regarde le planning d'une cat�gorie ou d'une ressource
        if ((myObjectView != null) && (!myObjectView.equals("myReservation"))
            && (!myObjectView.equals("PlanningOtherUser"))
            && (!myObjectView.equals("viewUser"))) {
          List listReservationsOfCategory = resourcesManagerSC
              .getMonthReservationOfCategory(myObjectView);
          List listResourcesofCategory = resourcesManagerSC
              .getResourcesByCategory(myObjectView);
          if (StringUtil.isDefined(request.getParameter("resourceId"))) {
            request.setAttribute("resourceId", request
                .getParameter("resourceId"));
          }
          request.setAttribute("listResourcesofCategory",
              listResourcesofCategory);
          request.setAttribute("listReservationsOfCategory",
              listReservationsOfCategory);
        }
        // on regarde les r�servations de quelqu'un :
        // si userId = null on regarde ses r�servations, sinon on regarde les
        // r�servations de cette personne
        else {
          List listOfReservation = null;
          if ((myObjectView != null)
              && (myObjectView.equals("PlanningOtherUser"))) {
            // on r�cup�re les r�servations de l'utilisateur, ainsi que son nom
            // et prenom
            if (idUser != null)
              resourcesManagerSC.setObjectViewForCalandar(idUser);
            else
              idUser = resourcesManagerSC.getObjectViewForCalandar();

            listOfReservation = resourcesManagerSC.getMonthReservation(idUser);
            request.setAttribute("firstNameUser", resourcesManagerSC
                .getFirstNameUserCalandar());
            request.setAttribute("lastName", resourcesManagerSC
                .getLastNameUserCalandar());
          } else {
            // on regarde ses propres r�servations
            listOfReservation = resourcesManagerSC.getMonthReservation();
          }
          request.setAttribute("listOfReservation", listOfReservation);
        }
        // initialisation d'un MonthCalendar du viewgenerator
        MonthCalendar monthC = resourcesManagerSC.getMonthCalendar();
        List listOfCategories = resourcesManagerSC.getCategories();
        request.setAttribute("idUser", idUser);
        request.setAttribute("listOfCategories", listOfCategories);
        request.setAttribute("idCategory", myObjectView);
        request.setAttribute("monthC", monthC);
        destination = root + "almanach.jsp";
      } else if (function.equals("PreviousMonth")) {
        resourcesManagerSC.previousMonth();
        destination = getDestination("Calendar", componentSC, request);
      } else if (function.equals("NextMonth")) {
        resourcesManagerSC.nextMonth();
        destination = getDestination("Calendar", componentSC, request);
      } else if (function.equals("GoToday")) {
        resourcesManagerSC.today();
        destination = getDestination("Calendar", componentSC, request);
      } else if (function.equals("Comments")) {
        String provenance = resourcesManagerSC.getProvenanceResource();
        String idResource = resourcesManagerSC.getResourceIdForResource();

        ResourceDetail myResource = resourcesManagerSC.getResource(idResource);

        request.setAttribute("Url", componentSC.getComponentUrl());
        request.setAttribute("resourceId", idResource);
        request.setAttribute("provenance", provenance);
        request.setAttribute("resource", myResource);

        destination = root + "comments.jsp";
      } else if (function.equals("SelectValidator")) {
        destination = resourcesManagerSC.initUPToSelectValidator("");
      } else if (function.startsWith("ChooseOtherPlanning")) {
        destination = resourcesManagerSC.initUserPanelOtherPlanning();
      } else if (function.startsWith("ViewOtherPlanning")) {
        // userPanel return
        UserDetail selectedUser = resourcesManagerSC.getSelectedUser();
        String idUser = selectedUser.getId();
        String firstNameUser = selectedUser.getFirstName();
        String lastName = selectedUser.getLastName();
        request.setAttribute("firstNameUser", firstNameUser);
        request.setAttribute("lastName", lastName);
        resourcesManagerSC.setFirstNameUserCalandar(firstNameUser);
        resourcesManagerSC.setLastNameUserCalandar(lastName);
        request.setAttribute("userId", idUser);
        // on indique au rooter qu'on regarde le calandrier de quelqu un
        request.setAttribute("objectView", "PlanningOtherUser");
        destination = getDestination("Calendar", componentSC, request);
      } else if (function.startsWith("searchResult")) {
        // traitement des recherches
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");
        if ("Reservation".equals(type)) {
          // traitement des r�servations
          request.setAttribute("reservationId", id);
          destination = getDestination("ViewReservation", resourcesManagerSC,
              request);
        } else if ("Category".equals(type)) {
          request.setAttribute("objectView", id);
          destination = getDestination("Calendar", resourcesManagerSC, request);
        } else if ("Resource".equals(type)) {
          request.setAttribute("resourceId", id);
          request.setAttribute("provenance", "calendar");
          destination = getDestination("ViewResource", resourcesManagerSC,
              request);
        }
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("resourcesManager",
        "ResourcesManagerRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  // recherche du profile de l'utilisateur
  public String getFlag(String[] profiles) {
    String flag = "user";
    for (int i = 0; i < profiles.length; i++) {
      if (profiles[i].equals("admin")) {
        return profiles[i];
      }
      if (profiles[i].equals("publisher")) {
        flag = profiles[i];
      } else if (profiles[i].equals("writer")) {
        if (!flag.equals("publisher")) {
          flag = profiles[i];
        }
      }
    }
    return flag;
  }

  private void setXMLFormIntoRequest(HttpServletRequest request,
      ResourcesManagerSessionController resourcesManagerSC) throws Exception {
    String idResource = resourcesManagerSC.getResourceIdForResource();
    String idCategory = resourcesManagerSC.getCategoryIdForResource();
    CategoryDetail category = resourcesManagerSC.getCategory(idCategory);
    String xmlFormName = category.getForm();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName = xmlFormName.substring(
          xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
      // cr�ation du PublicationTemplate
      PublicationTemplateManager.addDynamicPublicationTemplate(
          resourcesManagerSC.getComponentId() + ":" + xmlFormShortName,
          xmlFormName);
      PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) PublicationTemplateManager
          .getPublicationTemplate(resourcesManagerSC.getComponentId() + ":"
              + xmlFormShortName, xmlFormName);

      // cr�ation du formulaire et du DataRecord
      Form formUpdate = pubTemplate.getUpdateForm();
      RecordSet recordSet = pubTemplate.getRecordSet();
      // attention ici ce n est pas categoryId mais resourceId
      DataRecord data = recordSet.getRecord(idResource);
      if (data == null) {
        data = recordSet.getEmptyRecord();
        data.setId(idResource);
      }
      // appel de la jsp avec les param�tres
      request.setAttribute("Form", formUpdate);
      request.setAttribute("Data", data);
      request.setAttribute("XMLFormName", xmlFormName);
    }
  }

  private void putXMLDisplayerIntoRequest(ResourceDetail resource,
      HttpServletRequest request,
      ResourcesManagerSessionController resourcesManagerSC)
      throws PublicationTemplateException, FormException {
    // r�cup�ration de l�Id de l�objet en fonction de l�objet "object"
    String resourceId = resource.getId();
    String categoryId = resource.getCategoryId();
    CategoryDetail category = resourcesManagerSC.getCategory(categoryId);

    String xmlFormName = category.getForm();
    String xmlFormShortName = xmlFormName.substring(
        xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));

    // register xmlForm
    PublicationTemplateManager.addDynamicPublicationTemplate(resourcesManagerSC
        .getComponentId()
        + ":" + xmlFormShortName, xmlFormName);

    // cr�ation du PublicationTemplate
    PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) PublicationTemplateManager
        .getPublicationTemplate(resourcesManagerSC.getComponentId() + ":"
            + xmlFormShortName, xmlFormName);
    // r�cup�ration des donn�es
    Form formView = pubTemplate.getViewForm();
    RecordSet recordSet = pubTemplate.getRecordSet();
    DataRecord data = recordSet.getRecord(resourceId);
    if (data == null) {
      data = recordSet.getEmptyRecord();
      data.setId(resourceId);
    }
    // passage des param�tres � la request avec les donn�es du formulaire
    request.setAttribute("XMLForm", formView);
    request.setAttribute("XMLData", data);

    PagesContext context = new PagesContext("myForm", "0", resourcesManagerSC
        .getLanguage(), false, resourcesManagerSC.getComponentId(),
        resourcesManagerSC.getUserId());
    context.setBorderPrinted(false);
    context.setObjectId(resourceId);
    request.setAttribute("context", context);
  }

  private void updateXMLForm(
      ResourcesManagerSessionController resourcesManagerSC, List items)
      throws Exception {
    // r�cup�ration de l�objet et du nom du formulaire
    String idResource = resourcesManagerSC.getResourceIdForResource();
    String idCategory = resourcesManagerSC.getCategoryIdForResource();

    CategoryDetail category = resourcesManagerSC.getCategory(idCategory);

    String xmlFormName = category.getForm();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName = xmlFormName.substring(
          xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
      // r�cup�ration des donn�es du formulaire (via le DataRecord)
      PublicationTemplate pub = PublicationTemplateManager
          .getPublicationTemplate(resourcesManagerSC.getComponentId() + ":"
              + xmlFormShortName);
      RecordSet set = pub.getRecordSet();
      Form form = pub.getUpdateForm();
      DataRecord data = set.getRecord(idResource);
      if (data == null) {
        data = set.getEmptyRecord();
        data.setId(idResource);
      }

      // sauvegarde des donn�es du formulaire
      PagesContext context = new PagesContext("myForm", "0", resourcesManagerSC
          .getLanguage(), false, resourcesManagerSC.getComponentId(),
          resourcesManagerSC.getUserId());
      context.setObjectId(idResource);
      form.update(items, data, context);
      set.save(data);
    }
  }

}
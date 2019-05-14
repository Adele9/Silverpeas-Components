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

(function() {

  var replacementAsyncComponentRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/processManager/jsp/javaScript/vuejs/replacement-templates.jsp');

  var __activateRights = function(replacement) {
    replacement.canBeModified = true;
    replacement.canBeDeleted = true;
  };

  Vue.component('workflow-replacement-module',
    replacementAsyncComponentRepository.get('module', {
      mixins : [VuejsApiMixin, VuejsProgressMessageMixin],
      inject : ['context', 'replacementService'],
      data : function() {
        return {
          replacementApi : undefined,
          incumbentList : undefined,
          substituteList : undefined,
          allList : undefined
        };
      },
      created : function() {
        this.extendApiWith({
          add : function() {
            this.replacementApi.add();
          },
          reload : function() {
            var __promises = [];
            __promises.push(
                this.replacementService.getAllAsIncumbent(this.context.currentUser.id).then(
                    function(replacements) {
                      replacements.forEach(__activateRights);
                      this.incumbentList = replacements;
                    }.bind(this)));
            __promises.push(
                this.replacementService.getAllAsSubstitute(this.context.currentUser.id).then(
                    function(replacements) {
                      if (this.context.currentUser.isSupervisor) {
                        replacements.forEach(__activateRights);
                      }
                      this.substituteList = replacements;
                    }.bind(this)));
            if (this.context.currentUser.isSupervisor) {
              __promises.push(
                  this.replacementService.getAll(this.context.currentUser.id).then(
                      function(replacements) {
                        replacements.forEach(__activateRights);
                        this.allList = replacements;
                      }.bind(this)));
            }
            return sp.promise.whenAllResolved(__promises)
                .then(this.hideProgressMessage, this.hideProgressMessage);
          }
        });
        this.api.reload();
      },
      computed : {
        fullDisplay : function() {
          return this.context.currentUser.isSupervisor;
        }
      }
    }));

  var ReplacementEntityMixin = {
    computed : {
      isCreation : function() {
        return StringUtil.isNotDefined(this.replacement.uri);
      }
    }
  };

  Vue.component('workflow-replacement-management',
    replacementAsyncComponentRepository.get('management', {
      mixins : [VuejsApiMixin, VuejsI18nTemplateMixin, ReplacementEntityMixin, VuejsProgressMessageMixin],
      inject : ['context', 'replacementService'],
      data : function() {
        return {
          replacement : {},
          popinOpenDeferred : undefined,
          addPopinApi : undefined,
          addFormApi : undefined,
          modifyPopinApi : undefined,
          modifyFormApi : undefined,
          deletePopinApi : undefined
        };
      },
      created : function() {
        this.extendApiWith({
          view : function(replacementToView) {
            notyReset();
            this.replacement = replacementToView;
          },
          add : function() {
            this.api.modify()
          },
          modify : function(replacementToModify) {
            notyReset();
            jQuery.popup.showWaiting();
            this.popinOpenDeferred = sp.promise.deferred();
            this.replacement =
                extendsObject(this.replacementService.getTools().newReplacementEntity(),
                    replacementToModify);
            if (this.isCreation) {
              this.replacement.incumbent.id = this.context.currentUser.id;
              this.addPopinApi.open({
                openPromise : this.popinOpenDeferred.promise,
                callback : function() {
                  return this.addFormApi.validate().then(function(replacementToCreate) {
                    return this.saveReplacement(replacementToCreate);
                  }.bind(this));
                }.bind(this)
              });
            } else {
              this.modifyPopinApi.open({
                openPromise : this.popinOpenDeferred.promise,
                callback : function() {
                  return this.modifyFormApi.validate().then(function(replacementToModify) {
                    return this.saveReplacement(replacementToModify);
                  }.bind(this));
                }.bind(this)
              });
            }
            this.popinOpenDeferred.promise.then(jQuery.popup.hideWaiting, jQuery.popup.hideWaiting);
          },
          remove : function(replacementToDelete) {
            notyReset();
            this.popinOpenDeferred = undefined;
            this.replacement = replacementToDelete;
            this.deletePopinApi.open({
              callback : function() {
                return this.deleteReplacement(replacementToDelete);
              }.bind(this)
            });
          }
        });
      },
      methods : {
        saveReplacement : function(replacement) {
          this.showProgressMessage();
          return this.replacementService.saveReplacement(replacement).then(function(replacement) {
            if (this.isCreation) {
              this.$emit('replacement-create', replacement)
            } else {
              this.$emit('replacement-update', replacement)
            }
          }.bind(this))['catch'](this.hideProgressMessage);
        },
        deleteReplacement : function(replacement) {
          this.showProgressMessage();
          return this.replacementService.deleteReplacement(replacement).then(function() {
            this.$emit('replacement-delete', replacement)
          }.bind(this))['catch'](this.hideProgressMessage);
        }
      }
    }));

  Vue.component('workflow-replacement-form',
    replacementAsyncComponentRepository.get('form', {
      mixins : [VuejsFormApiMixin, VuejsI18nTemplateMixin, ReplacementEntityMixin],
      inject : ['context', 'rootFormMessages'],
      props : {
        replacement : Object,
        contentReadyDeferred : Object
      },
      data : function() {
        return {
          selectIncumbentApi : undefined,
          selectSubstituteApi : undefined
        };
      },
      created : function() {
        this.extendApiWith({
          validateForm : function() {
            var data = this.replacement;
            if (data.substitute.id === data.incumbent.id) {
              this.rootFormApi.errorMessage().add(
                  this.formatMessage(this.rootFormMessages.mustBeDifferentFrom,
                      [this.messages.substituteLabel, this.messages.incumbentLabel]));
            }
            if (sp.moment.make(data.endDate).isBefore(sp.moment.make(data.startDate))) {
              this.rootFormApi.errorMessage().add(
                  this.formatMessage(this.rootFormMessages.correctEndDateIncludedPeriod,
                      [this.messages.startDateLabel, this.messages.endDateLabel]));
            }
            return this.rootFormApi.errorMessage().none();
          },
          updateFormData : function(replacementToUpdate) {
            extendsObject(replacementToUpdate, this.replacement);
          }
        });
      },
      watch : {
        replacement : function() {
          Vue.nextTick(function() {
            var __promises = [];
            __promises.push(this.selectIncumbentApi.refresh());
            __promises.push(this.selectSubstituteApi.refresh());
            sp.promise.whenAllResolved(__promises).then(function() {
              this.contentReadyDeferred.resolve();
              this.selectSubstituteApi.focus();
            }.bind(this));
          }.bind(this));
        }
      },
      methods : {
        incumbentChanged : function(userIds) {
          this.replacement.incumbent.id = userIds.length ? userIds[0] : '';
        },
        substituteChanged : function(userIds) {
          this.replacement.substitute.id = userIds.length ? userIds[0] : '';
        }
      },
      computed : {
        roleFilter : function() {
          var roles = [];
          if (this.context.currentUser.isSupervisor) {
            for (var roleName in this.context.replacementHandledRoles) {
              roles.push(roleName);
            }
          } else {
            roles.push(this.context.currentUser.role);
          }
          return roles;
        }
      }
    }));

  Vue.component('workflow-replacement-list-item',
    replacementAsyncComponentRepository.get('list-item', {
      props : {
        replacement : Object
      },
      computed : {
        isOneDay : function() {
          return this.replacement.startDate === this.replacement.endDate;
        }
      }
    }));

  Vue.component('workflow-replacement-list-item-actions',
    replacementAsyncComponentRepository.get('list-item-actions', {
      props : {
        replacement : {
          'type' : Object,
          'default' : function() {
            return {};
          }
        }
      }
    }));
})();
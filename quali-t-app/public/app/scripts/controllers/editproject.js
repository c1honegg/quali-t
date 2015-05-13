'use strict';

/**
 * @ngdoc function
 * @name qualitApp.controller:EditProjectCtrl
 * @description
 * # EditprojectCtrl
 * Controller of the qualitApp
 */
angular.module('qualitApp')
  .controller('EditProjectCtrl', function ($scope, $stateParams, apiService, favoritesService, alerts) {
    $scope.projectId = $stateParams.projectId;
    $scope.project = {};
    $scope.favoriteProjects = new Array();
    $scope.selectedCustomer = {};
    $scope.customerList = new Array();
    $scope.selectedQualityProperties = new Array();
    $scope.qualityPropertiesList = new Array();
    $scope.isProjectFavorite = false;
    $scope.tooltipsSave = "Saves the project and shows warnings (statistics & fuzzyness) if there are any.";
    $scope.tooltipsValidate = "Validate the project's quality attributes for statistics and " +
    "fuzzyness will show you open issues and suggesstions.";
    $scope.tooltipsExportToIssueTracker = "Export selected quality attributes to issue tracking system.";
    $scope.tooltipsExport = "Select the quality attributes you want to export to issue tracking system.";

    $scope.checkIsFavorite = function (projectId, favoriteProjects) {
      return favoritesService.isProjectFavorite(projectId, favoriteProjects);
    }

    $scope.addToFavorites = function (projectId) {
      $scope.changeState(projectId, true);
    }

    $scope.removeFromFavorites = function (projectId) {
      $scope.changeState(projectId, false);
    }

    $scope.changeState = function (projectId, isFavorite) {
      var promiseQualityProperties = apiService.changeState(projectId, isFavorite);
      promiseQualityProperties.then(
        function (payload) {
          $scope.favoriteProjects = payload.data.favorites;
          $scope.isProjectFavorite = $scope.checkIsFavorite($scope.projectId, $scope.favoriteProjects);
        });
    }

    $scope.save = function () {
      // get only needed information
      var project = {
        id: $scope.project.id,
        name: $scope.project.name,
        jirakey: $scope.project.jirakey,
        customer: $scope.selectedCustomer.id,
        qualityProperties: $scope.selectedQualityProperties
      };

      var promiseSave = apiService.updateProject(project);
      promiseSave.then(
        function (payload) {
          alerts.createSuccess("Project was successfully updated.");
          $scope.project = payload.data;
        });
    }

    $scope.validate = function () {

    }

    $scope.exportToIssueTracker = function () {

    }

    $scope.init = function () {
      var promiseInit = apiService.getFavorites();
      promiseInit.then(
        function (payload) {
          $scope.favoriteProjects = payload.data;
          return apiService.getProject($scope.projectId);
        }).then(
        function (payload) {
          $scope.project = payload.data;
          $scope.selectedCustomer = $scope.project.projectCustomer;
          $scope.selectedQualityProperties = $scope.project.qualityProperties;
          $scope.isProjectFavorite = $scope.checkIsFavorite($scope.projectId, $scope.favoriteProjects);
          return apiService.getCustomers();
        }).then(
        function (payload) {
          $scope.customerList = payload.data;
          return apiService.getQualityProperties();
        }).then(
        function (payload) {
          $scope.qualityPropertiesList = payload.data;
        }
      );
    }
  });
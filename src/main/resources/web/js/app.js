var app = angular.module("iteraVentory", ["smartTable.table"]);

app.factory("inventory", function($http, $q) {

	var inventory = $q.defer();

	this.fetch = function() {
		$http.get("/inventory").success(function(data) {
			inventory.resolve(data);
		}).error(function(data, status) {
			inventory.reject(status);
		});
		return inventory.promise;
	}

	var columns = $q.defer();

	this.columns = function() {
		$http.get("/inventory/columns").success(function(data) {
			columns.resolve(data);
		}).error(function(data, status) {
			columns.reject(status);
		});
		return columns.promise;
	}

	return this;
});
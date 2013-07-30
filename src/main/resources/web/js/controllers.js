function Main($scope, $location, $log, $timeout, inventory) {

	$scope.columnCollection = [];
	$scope.rowCollection = [];

	inventory.columns().then(function(columns) {
		$scope.columnCollection = columns;
	});

	inventory.fetch().then(function(rows) {
		$scope.rowCollection = rows;
	});

	$scope.globalConfig = {
		isGlobalSearchActivated : true,
		filterAlgorithm : function(inventory, filter) {
			var filterValue = filter["$"];
			if (!filterValue) {
				return inventory;
			}
			var terms = filterValue.split(" ");
			function containsTerm(machine, index, inventory) {
				var contains = true;
				for (term in terms) {
					contains = contains && hasValue(machine, terms[term]);
				}
				return contains;
			};
			var filteredArray = inventory.filter(containsTerm);
			return filteredArray;
		},
		isPaginationEnabled : false
	}

	function hasValue(machine, term) {
		var result = false;
		for (prop in machine) {
			if (machine[prop] && machine[prop].search(term) != -1) {
				result = true;
			}
		}
		return result;
	}

}
'use strict';

describe('Directive: toggleRoleBtn', function () {

  // load the directive's module
  beforeEach(module('qualitApp'));

  var scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('todo', inject(function () {
    expect('todo').toBe('todo');
  }));
});

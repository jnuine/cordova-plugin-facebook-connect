'use strict';

function isLoggedIn (success, fail) {
  cordova.exec(
    success,
    fail,
    'FacebookConnect',
    'isLoggedIn',
    []
  );
}

function logInWithReadPermissions (permissions, success, fail) {
  cordova.exec(
    success,
    fail,
    'FacebookConnect',
    'logInWithReadPermissions',
    permissions
  );
}

function logInWithPublishPermissions (permissions, success, fail) {
  cordova.exec(
    success,
    fail,
    'FacebookConnect',
    'logInWithPublishPermissions',
    permissions
  );
}

function getUserInfo (success, fail) {
  cordova.exec(
    success,
    fail,
    'FacebookConnect',
    'getUserInfo',
    []
  );
}

function logout (success) {
  cordova.exec(
    success,
    function () {}, // never happens
    'FacebookConnect',
    'logout',
    []
  );
}

module.exports = {
  isLoggedIn: isLoggedIn,
  logInWithReadPermissions: logInWithReadPermissions,
  logInWithPublishPermissions: logInWithPublishPermissions,
  getUserInfo: getUserInfo,
  logout: logout
};

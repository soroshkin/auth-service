let confirmPassword = function () {
    if (document.getElementById('password').value ===
        document.getElementById('repeatPassword').value) {
        document.getElementById('message').style.color = '#73AD21';
        document.getElementById('message').innerHTML = 'OK';
    } else {
        document.getElementById('message').style.color = 'red';
        document.getElementById('message').innerHTML = 'not matching';
    }
};
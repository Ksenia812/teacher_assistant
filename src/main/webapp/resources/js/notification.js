Notification.requestPermission().then(function (result) {
	console.log(result);
});

function spawnNotification(notification) {
	let options = {
		body: notification.title.toUpperCase().concat("\n", notification.body.toUpperCase()),
		icon: notification.image
	};
	let n = new Notification("", options);
	setTimeout(n.close.bind(n), notification.timeout);
}

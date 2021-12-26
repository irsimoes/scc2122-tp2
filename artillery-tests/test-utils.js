'use strict';

/***
 * Exported functions to be used in the testing scripts.
 */
module.exports = {
	uploadImageBody,
	genNewUser,
	genNewUserReply,
	selectUser,
	selectUserSkewed,
	genNewChannel,
	selectChannelFromUser,
	selectChannelFromUserSkewed,
	selectChannelFromChannelLst,
	selectChannelFromChannelLstSkewed,
	genNewMessage,
	selectImagesIdFromMsgList,
	random50,
	random70,
	random80,
	deleteUserReply,
	deleteChannelReply,
	selectChannelSkewed,
	genNewChannelReply,
	genNewMessageReply,
	selectMessageSkewed,
	getUserPassword,
	deleteMessageReply,
	selectMessageFromMessageLst
}

const Faker = require('faker')
const fs = require('fs')
const path = require('path')

var imagesIds = []
var images = []
var users = []
var channels = []
let messages = []

// All endpoints starting with the following prefixes will be aggregated in the same for the statistics
var statsRegExpr = [[/.*\/rest\/media\/.*/, "GET", "/rest/media/*"],
[/.*\/rest\/media/, "POST", "/rest/media"],
[/.*\/rest\/users\/.*\/channels/, "GET", "/rest/users/*/channels"],
[/.*\/rest\/users\/.*/, "GET", "/rest/users/*"],
[/.*\/rest\/channels\/.*\/subscribe\/.*/, "PUT", "/rest/users/*/subscribe/*"],
[/.*\/rest\/users\/auth/, "POST", "/rest/users/auth"],
[/.*\/rest\/users/, "POST", "/rest/users"],
[/.*\/rest\/users/, "PUT", "/rest/users"],
[/.*\/rest\/channels\/.*\/add\/.*/, "PUT", "/rest/channels/*/add/*"],
[/.*\/rest\/channels/, "POST", "/rest/channels"],
[/.*\/rest\/channels\/.*\/messages.*/, "GET", "/rest/channels/*/messages"],
[/.*\/rest\/channels\/.*/, "GET", "/rest/channels/*"],
[/.*\/rest\/messages/, "POST", "/rest/messages"],
[/.*\/rest\/messages\/.*/, "GET", "/rest/messages/*"],
[/.*\/rest\/channels\/.*/, "DELETE", "/rest/channels/*"],
]

// Function used to compress statistics
global.myProcessEndpoint = function (str, method) {
	var i = 0;
	for (i = 0; i < statsRegExpr.length; i++) {
		if (method == statsRegExpr[i][1] && str.match(statsRegExpr[i][0]) != null)
			return method + ":" + statsRegExpr[i][2];
	}
	return method + ":" + str;
}

// Auxiliary function to select an element from an array
Array.prototype.sample = function () {
	return this[Math.floor(Math.random() * this.length)]
}

// Auxiliary function to select an element from an array
Array.prototype.sampleSkewed = function () {
	return this[randomSkewed(this.length)]
}

// Returns a random value, from 0 to val
function random(val) {
	return Math.floor(Math.random() * val)
}

// Returns a random value, from 0 to val
function randomSkewed(val) {
	let beta = Math.pow(Math.sin(Math.random() * Math.PI / 2), 2)
	let beta_left = (beta < 0.5) ? 2 * beta : 2 * (1 - beta);
	return Math.floor(beta_left * val)
}


// Loads data about images from disk
function loadData() {
	var basedir
	if (fs.existsSync('/images'))
		basedir = '/images'
	else
		basedir = 'images'
	fs.readdirSync(basedir).forEach(file => {
		if (path.extname(file) === ".jpeg") {
			var img = fs.readFileSync(basedir + "/" + file)
			images.push(img)
		}
	})
	var str;
	if (fs.existsSync('users.data')) {
		str = fs.readFileSync('users.data', 'utf8')
		users = JSON.parse(str)
	}
	if (fs.existsSync('channels.data')) {
		str = fs.readFileSync('channels.data', 'utf8')
		channels = JSON.parse(str)
	}
	if (fs.existsSync('messages.data')) {
		str = fs.readFileSync('messages.data', 'utf8')
		messages = JSON.parse(str)
	}
}

loadData();

/**
 * Sets the body to an image, when using images.
 */
function uploadImageBody(requestParams, context, ee, next) {
	requestParams.body = images.sample()
	return next()
}

/**
 * Process reply of the download of an image. 
 * Update the next image to read.
 */
function processUploadReply(requestParams, response, context, ee, next) {
	if (typeof response.body !== 'undefined' && response.body.length > 0) {
		imagesIds.push(response.body)
	}
	return next()
}

/**
 * Select an image to download.
 */
function selectImageToDownload(context, events, done) {
	if (imagesIds.length > 0) {
		context.vars.imageId = imagesIds.sample()
	} else {
		delete context.vars.imageId
	}
	return done()
}


/**
 * Generate data for a new user using Faker
 */
function genNewUser(context, events, done) {
	const first = `${Faker.name.firstName()}`
	const last = `${Faker.name.lastName()}`
	context.vars.id = first + "." + last + "." + Date.now()
	context.vars.name = first + " " + last
	context.vars.pwd = `${Faker.internet.password()}`
	return done()
}


/**
 * Process reply for of new users to store the id on file
 */
function genNewUserReply(requestParams, response, context, ee, next) {
	if (response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0) {
		let u = JSON.parse(response.body)
		users.push(u)
		fs.writeFileSync('users.data', JSON.stringify(users));
	}
	return next()
}


/**
 * Process reply for of new channels to store the id on file
 */
function genNewChannelReply(requestParams, response, context, ee, next) {
	if (response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0) {
		let c = JSON.parse(response.body)
		channels.push(c)
		fs.writeFileSync('channels.data', JSON.stringify(channels));
	}
	return next()
}

function genNewMessageReply(requestParams, response, context, ee, next) {
	if (response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0) {
		let message = {
			"id": response.body,
			"channel": context.vars.channelId,
			"user": context.vars.user,
			"text": context.vars.msgText,
			"imageId": context.vars.imageId
		}
		messages.push(message);
		fs.writeFileSync('messages.data', JSON.stringify(messages));
	}
	return next();
}


function deleteUserReply(requestParams, response, context, ee, next) {
	if (response.statusCode >= 200 && response.statusCode < 300) {
		//apagar os users
		users = users.filter(function (currentValue, index, arr) {
			return currentValue.id !== context.vars.user;
		});
		fs.writeFileSync('users.data', JSON.stringify(users));

		//apagar os channels que sao desse user 
		filterChannelsDelByUser(context.vars.user)

		//apagar as msgs q sao desse user
		filterMessagesDelByUser(context.vars.user)

	}
	return next()
}

function filterChannelsDelByUser(userId) {
	if (channels.length > 0) {
		channels = channels.filter(function (currentValue, index, arr) {
			return currentValue.owner !== userId;
		});
		fs.writeFileSync('channels.data', JSON.stringify(channels));
	}
}

function filterMessagesDelByUser(userId) {
	if (messages.length > 0) {
		messages = messages.filter(function (currentValue, index, arr) {
			return currentValue.user !== userId;
		});
		fs.writeFileSync('messages.data', JSON.stringify(messages));
	}
}

function deleteChannelReply(requestParams, response, context, ee, next) {
	if (response.statusCode >= 200 && response.statusCode < 300) {
		channels = channels.filter(function (value, index, arr) {
			return value.id != context.vars.channel;
		});
		fs.writeFileSync('channels.data', JSON.stringify(channels));

		//apagar as mensagens
		messages = messages.filter(function (currentValue, index, arr) {
			return currentValue.channel !== context.vars.channel;
		});
		fs.writeFileSync('messages.data', JSON.stringify(messages));

	}
	return next()
}

function deleteMessageReply(requestParams, response, context, ee, next) {
	if (response.statusCode >= 200 && response.statusCode < 300) {
		messages = messages.filter(function (value, index, arr) {
			return value.id != context.vars.id;
		});
		fs.writeFileSync('messages.data', JSON.stringify(messages));
	}

	return next();
}



/**
 * Select user
 */
function selectUser(context, events, done) {
	if (users.length > 0) {
		let user = users.sample()
		context.vars.user = user.id
		context.vars.pwd = user.pwd
	} else {
		delete context.vars.user
		delete context.vars.pwd
	}
	return done()
}


/**
 * Select user
 */
function selectUserSkewed(context, events, done) {
	if (users.length > 0) {
		let user = users.sampleSkewed()
		context.vars.user = user.id
		context.vars.pwd = user.pwd
	} else {
		delete context.vars.user
		delete context.vars.pwd
	}
	return done()
}

/**
 * Select user
 */
function selectMessageSkewed(context, events, done) {
	if (messages.length > 0) {
		let message = messages.sampleSkewed()
		context.vars.user = message.user
		context.vars.id = message.id
	} else {
		delete context.vars.user
		delete context.vars.pwd
	}
	return done()
}

/**
 * Select user
 */
function selectChannelSkewed(context, events, done) {
	if (channels.length > 0) {
		let channel = channels.sampleSkewed()
		context.vars.channel = channel.id
		context.vars.user = channel.owner
		let user = users.filter(function (value, index, arr) {
			return value.id == channel.owner;
		}).pop();
		context.vars.pwd = user.pwd
	} else {
		delete context.vars.channel
	}
	return done()
}

/**
 * Generate data for a new channel
 */
function genNewChannel(context, events, done) {
	context.vars.channelName = `${Faker.random.word()}`
	context.vars.publicChannel = Math.random() < 0.2
	return done()
}



/**
 * Select a channel from the list of channelIds in a user
 */
function selectChannelFromUser(context, events, done) {
	if (typeof context.vars.userObj !== 'undefined' && context.vars.userObj.channelIds !== 'undefined' &&
		context.vars.userObj.channelIds.length > 0)
		context.vars.channelId = context.vars.userObj.channelIds.sample()
	else
		delete context.vars.channelId
	return done()
}

/**
 * Select a channel from the list of channelIds in a user
 */
function selectChannelFromUserSkewed(context, events, done) {
	if (typeof context.vars.userObj !== 'undefined' && context.vars.userObj.channelIds !== 'undefined' &&
		context.vars.userObj.channelIds.length > 0)
		context.vars.channelId = context.vars.userObj.channelIds.sampleSkewed()
	else
		delete context.vars.channelId
	return done()
}

/**
 * Select a channel from the list of channelIds in a user
 */
function selectChannelFromChannelLst(context, events, done) {
	if (typeof context.vars.channelLst !== 'undefined' && context.vars.channelLst.length > 0)
		context.vars.channelId = context.vars.channelLst.sample()
	else
		delete context.vars.channelId
	return done()
}

/**
 * Select a channel from the list of channelIds in a user
 */
function selectChannelFromChannelLstSkewed(context, events, done) {
	if (typeof context.vars.channelLst !== 'undefined' && context.vars.channelLst.length > 0)
		context.vars.channelId = context.vars.channelLst.sampleSkewed()
	else
		delete context.vars.channelId
	return done()
}

function selectMessageFromChannel(context, events, done) {
	if (typeof context.vars.userObj !== 'undefined' && context.vars.userObj.channelIds !== 'undefined' &&
		context.vars.userObj.channelIds.length > 0)
		context.vars.channelId = context.vars.userObj.channelIds.sample()
	else
		delete context.vars.channelId
	return done()
}

/**
 * Select a channel from the list of channelIds in a user
 */
function selectMessageFromChannelSkewed(context, events, done) {
	if (typeof context.vars.userObj !== 'undefined' && context.vars.userObj.channelIds !== 'undefined' &&
		context.vars.userObj.channelIds.length > 0)
		context.vars.channelId = context.vars.userObj.channelIds.sampleSkewed()
	else
		delete context.vars.channelId
	return done()
}

/**
 * Select a channel from the list of channelIds in a user
 */
function selectMessageFromMessageLst(context, events, done) {
	if (typeof context.vars.msgList !== 'undefined' && context.vars.msgList.length > 0)
		context.vars.msgId = context.vars.msgList.sample().id
	else
		delete context.vars.msgId
	return done()
}

/**
 * Select a channel from the list of channelIds in a user
 */
function selectChannelFromChannelLstSkewed(context, events, done) {
	if (typeof context.vars.channelLst !== 'undefined' && context.vars.channelLst.length > 0)
		context.vars.channelId = context.vars.channelLst.sampleSkewed()
	else
		delete context.vars.channelId
	return done()
}


/**
 * Generate data for a new message
 */
function genNewMessage(context, events, done) {
	context.vars.msgText = `${Faker.lorem.paragraph()}`
	if (Math.random() < 0.05) {
		context.vars.hasImage = true
	} else {
		delete context.vars.hasImage
	}
	context.vars.imageId = null
	return done()
}

/**
 * Select imageIds from msgList
 */
function selectImagesIdFromMsgList(context, events, done) {
	let imageIdLst = []
	if (typeof context.vars.msgList !== 'undefined') {
		let msg
		for (msg in context.vars.msgList) {
			if (msg.imageId != null) {
				if (!imageIdLst.includes(msg.imageId))
					imageIdLst.push(msg.imageId)
			}
		}
	}
	context.vars.imageIdLst = imageIdLst
	return done()
}

function getUserPassword(context, events, done) {
	let user = users.filter(function (currentValue, index, arr) {
		return currentValue.id == context.vars.user;
	}).pop();
	context.vars.pwd = user.pwd;
	return done();
}



/**
 * Return true with probability 50% 
 */
function random50(context, next) {
	const continueLooping = Math.random() < 0.5
	return next(continueLooping);
}

/**
 * Return true with probability 70% 
 */
function random70(context, next) {
	const continueLooping = Math.random() < 0.7
	return next(continueLooping);
}

function random80(context, next) {
	const continueLooping = Math.random() < 0.80
	return next(continueLooping);
}
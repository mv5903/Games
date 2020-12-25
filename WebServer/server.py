from flask import Flask
from flask_restful import Api, Resource, reqparse
import socket

app = Flask(__name__)
api = Api(app)

users = [
	{
		"name": "Matthew",
		"hand": [
			"6 Spades",
			"K Diamonds"
		],
		"nameOfHand": "None yet",
		"handCombinedWithCenter": [],
		"highestCard": "K Diamonds",
		"hasFolded": False,
		"bet": 0,
		"finishedRound": False
	},
	{
		"name": "Chris",
		"hand": [
			"A Hearts",
			"8 Spades"
		],
		"nameOfHand": "None yet",
		"handCombinedWithCenter": [],
		"highestCard": "K Hearts",
		"hasFolded": True,
		"bet": 0,
		"finishedRound": False
	}
]

class User(Resource):
	def get(self, name):
		for user in users:
			if (name == user["name"]):
				return user, 200
		return "User Not Found", 404

	def post(self, name):
		parser = reqparse.RequestParser()
		parser.add_argument("name")
		parser.add_argument("hand")
		parser.add_argument("nameOfHand")
		parser.add_argument("handCombinedWithCenter")
		parser.add_argument("highestCard")
		parser.add_argument("hasFolded")
		parser.add_argument("bet")
		parser.add_argument("finishedRound")
		args = parser.parse_args()

		for user in users:
			if (name == user["name"]):
				return "User with name {} already exists".format(name), 400

		user = {
			"name" : name,
			"hand" : args["hand"],
			"nameofHand" : args["nameOfHand"],
			"nameOfHand" : args["handCombinedWithCenter"],
			"highestCard" : args["highestCard"],
			"hasFolded" : args["hasFolded"],
			"bet" : args["bet"],
			"finishedRound" : args["finishedRound"]
		}

		users.append(user)
		return user, 201

	def put(self, name):
		parser = reqparse.RequestParser()
		parser.add_argument("name")
		parser.add_argument("hand")
		parser.add_argument("nameOfHand")
		parser.add_argument("handCombinedWithCenter")
		parser.add_argument("highestCard")
		parser.add_argument("hasFolded")
		parser.add_argument("bet")
		parser.add_argument("finishedRound")
		args = parser.parse_args()

		for user in users:
			if (name == user["name"]):
				user["hand"] : args["hand"]
				user["nameofHand"] : args["nameOfHand"]
				user["nameOfHand"] : args["handCombinedWithCenter"]
				user["highestCard"] : args["highestCard"]
				user["hasFolded"] : args["hasFolded"]
				user["bet"] : args["bet"]
				user["finishedRound"] : args["finishedRound"]
				return user, 200

		user = {
			"name" : name,
			"hand" : args["hand"],
			"nameofHand" : args["nameOfHand"],
			"nameOfHand" : args["handCombinedWithCenter"],
			"highestCard" : args["highestCard"],
			"hasFolded" : args["hasFolded"],
			"bet" : args["bet"],
			"finishedRound" : args["finishedRound"]
		}
		users.append(user)
		return user, 201

	def delete(self, name):
		global users
		users = [user for user in users if user["name"] != name]
		return "{} has been deleted.".format(name), 200

api.add_resource(User, "/user/<string:name>")

#Get ip address of user so that port forwarding works for use over the internet
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.connect(("8.8.8.8", 80))
local_ip = sock.getsockname()[0]
sock.close()

app.run(host=local_ip, port=5000, debug=True)


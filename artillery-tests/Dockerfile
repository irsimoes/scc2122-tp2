FROM node
WORKDIR /home/artillery
ADD images ~/artillery/images
ADD create-channels.yml ~/artillery
ADD create-messages.yml ~/artillery
ADD create-users.yml ~/artillery
ADD delete-channels.yml ~/artillery
ADD delete-messages.yml ~/artillery
ADD delete-users.yml ~/artillery
ADD workload1.yml ~/artillery
ADD workload2.yml ~/artillery
ADD workload3.yml ~/artillery
RUN npm install artillery
RUN npm install faker
RUN npm install node-fetch -save
RUN npm install https://github.com/preguica/artillery-plugin-metrics-by-endpoint.git
#EXPOSE 8080

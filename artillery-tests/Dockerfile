FROM node
WORKDIR /home/artillery
ADD images /home/artillery/images
ADD test-utils.js /home/artillery
ADD create-channels.yml /home/artillery
ADD create-messages.yml /home/artillery
ADD create-users.yml /home/artillery
ADD delete-channels.yml /home/artillery
ADD delete-messages.yml /home/artillery
ADD delete-users.yml /home/artillery
ADD workload1.yml /home/artillery
ADD workload2.yml /home/artillery
ADD workload3.yml /home/artillery
RUN npm install artillery
RUN npm install faker
RUN npm install node-fetch -save
RUN npm install https://github.com/preguica/artillery-plugin-metrics-by-endpoint.git
#CMD sleep 99999999
CMD ./node_modules/.bin/artillery run create-users.yml ; \
./node_modules/.bin/artillery run --output workload1.json workload1.yml ; \
./node_modules/.bin/artillery report --output /aci/logs/workload1.html workload1.json ; \
sleep 3; \
./node_modules/.bin/artillery run --output workload2.json workload2.yml ; \
./node_modules/.bin/artillery report --output /aci/logs/workload2.html workload2.json ; \
sleep 3 ; \
./node_modules/.bin/artillery run --output workload3.json workload3.yml ; \
./node_modules/.bin/artillery report --output /aci/logs/workload3.html workload3.json
#EXPOSE 8080

#!/bin/bash
docker stop $(sudo docker ps -a -q)
docker container prune -f
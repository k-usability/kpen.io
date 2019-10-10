* Queries
-  select * from job where request_dt>now()-interval '10 minutes' order by request_dt; 

* Deployment
- dockerize.sh 
- create-cluster-1.sh
- apply-k8s-dashboard.sh
- apply-workers.sh
- apply-server-elb.sh
- Go to AWS Route 53 to add A alias record for kpen.io to the load balancer

* References
    - https://aws.amazon.com/premiumsupport/knowledge-center/terminate-https-traffic-eks-acm/
    

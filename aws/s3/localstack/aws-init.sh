# S3
awslocal s3api create-bucket --bucket nl-portal --create-bucket-configuration LocationConstraint=eu-central-1

echo hello world > helloworld.txt
awslocal s3api put-object --bucket nl-portal --key helloworld.txt --body helloworld.txt
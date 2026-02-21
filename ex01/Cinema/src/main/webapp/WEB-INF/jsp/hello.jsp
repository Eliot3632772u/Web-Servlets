<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>hello jsp</title>
</head>
<body>
    <h1>hello jsp, the date is: <%= java.time.LocalDateTime.now() %></h1>
    <% 
        for(int i = 0;i < 5; i++)
        {
            System.out.println(i);
            out.println(i);
        }
    %>
</body>
</html>
import re

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'r') as f:
    content = f.read()

# First replace the state variables at the beginning
state_pattern = r"    var showManualEmailFlow.*?\n    var isAuthenticating by remember \{ mutableStateOf\(false\) \}\n"
content = re.sub(state_pattern, "", content, flags=re.DOTALL)

# Then remove the Manual Email button and the else block
# Let's just find "if (!showManualEmailFlow) {"
content = content.replace("                    if (!showManualEmailFlow) {", "")

# Manual Email Sign-In Button
start_manual_btn = "                        // Manual Email Sign-In Button"
end_manual_btn = "                        // Anonymous Sign-In Button"
start_idx = content.find(start_manual_btn)
end_idx = content.find(end_manual_btn)
if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + content[end_idx:]

# The end of the if block and the whole else block
start_else = "                        }\n                    } else {"
end_else = "                        }\n                    }\n                }\n            }"
# Let's replace the block ending
start_idx = content.find(start_else)
end_idx = content.find(end_else)
if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + "                        }\n                }\n            }" + content[end_idx + len(end_else):]

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'w') as f:
    f.write(content)

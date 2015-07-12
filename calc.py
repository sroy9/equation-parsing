from itertools import islice
class Stack(list):
    def push(self,item):
        self.append(item)
    def peek(self):
        return self[-1]
    def isEmpty(self):
        return not self

def infixToPrefix(infixexpr):
    # print "input is", infixexpr
    infixexpr=infixexpr[::-1]
    mystr=[]
    for c in infixexpr:
        if c == '(':
            mystr.append(')')
        elif c==')':
            mystr.append('(')
        else:
            mystr.append(c)
    # print "reverse is ",''.join(mystr)
    pp=infixToPostfix(''.join(mystr))
    return pp[::-1]

def infixToPostfix(infixexpr):
    # print "infix was", infixexpr
    prec = {}
    prec["*"] = 3
    prec["/"] = 3
    prec["+"] = 2
    prec["-"] = 2
    prec["("] = 1
    prec["="]=0
    opStack = Stack()
    postfixList = []
    tokenList = infixexpr.split()

    for token in tokenList:
        if token not in prec and token!=')':
            postfixList.append(token)
        # if token in "ABCDEFGHIJKLMNOPQRSTUVWXYZ" or token in "0123456789":
        #     postfixList.append(token)
        elif token == '(':
            opStack.push(token)
        elif token == ')':
            topToken = opStack.pop()
            while topToken != '(':
                postfixList.append(topToken)
                topToken = opStack.pop()
        else:
            while (not opStack.isEmpty()) and \
               (prec[opStack.peek()] >= prec[token]):
                  postfixList.append(opStack.pop())
            opStack.push(token)

    while not opStack.isEmpty():
        postfixList.append(opStack.pop())
    return " ".join(postfixList)

def make_lambda(expr):
    print "input is",expr
    expr=expr.replace(""," ")[1:-1]
    prefixExpr=infixToPrefix(expr)    
    prec = {}
    prec["*"] = 3
    prec["/"] = 3
    prec["+"] = 2
    prec["-"] = 2
    prec["("] = 1
    prec["="]=0
    tokenList = prefixExpr.split()
    opStack = Stack(tokenList)
    bufferSt = Stack()
    while not opStack.isEmpty():
        tok=opStack.pop()
        if tok in prec:
            arg1=bufferSt.pop()
            arg2=bufferSt.pop()
            bufferSt.push(" ("+tok+arg1+" "+arg2+") ")
        else:
            bufferSt.push(tok)
    out = bufferSt[0]
    out = out.replace("*","lambda.mult ")
    out = out.replace("+","lambda.add ")
    out = out.replace("-","lambda.sub ")
    out = out.replace("/","lambda.div ")
    out = out.replace("=","lambda.eq ")
    print out
            # print(infixToPostfix("A * B + C * D"))
# print(infixToPostfix("( A + B ) * C - ( D - E ) * ( F + G )"))
# print(infixToPrefix("A + ( B * C - ( D / E - F ) * G ) * H"))
# print(infixToPrefix("A * B = C * D"))
# print(infixToPrefix("A * B + C * D"))
# print(infixToPrefix("( A + B ) * C - ( D - E ) * ( F + G )"))
# print(infixToPrefix("( A + B ) * C = ( D - E ) * ( F + G )"))
make_lambda("A * B = C * D")
make_lambda("( A + B ) * C = ( D - E ) * ( F + G )")
make_lambda("X=2*Y-1")
# make_lambda("2*X-(-8)=(-12)")

if __name__=="__main__":
    lines=open("equationparse1.txt").readlines()
    print len(lines)
    # lines=lines[1:]
    # for i in islice(lines,0,len(lines),2):
    #     print i,
    for line in lines:
        if "=" in line:
            line=line.strip()
            line=line.replace("V1","x")
            line=line.replace("V2","y")
            make_lambda(line)

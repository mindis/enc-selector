import numpy as np

from ndnn.node import Concat, Sigmoid, Embed, Dot, Tanh, Add, Mul, SoftMax, Collect, ArgMax

from ndnn.graph import Graph
from ndnn.init import Zero
from ndnn.loss import LogLoss
from ndnn.sgd import SGD

class LSTMCell(object):
    def __init__(self, wf, bf, wi, bi, wc, bc, wo, bo, x, h, c):
        self.wf = wf
        self.bf = bf
        self.wi = wi
        self.bi = bi
        self.wc = wc
        self.bc = bc
        self.wo = wo
        self.bo = bo
        self.x = x
        self.h = h
        self.c = c
  
        concat = Concat(h, x)
        fgate = Sigmoid(Add(Dot(concat, wf), bf))
        igate = Sigmoid(Add(Dot(concat, wi), bi))
        cgate = Mul(Tanh(Add(Dot(concat, wc), bc)), igate)
        ogate = Sigmoid(Add(Dot(concat, wo), bo))

        self.cout = Add(Mul(c, fgate), cgate)
        self.hout = Mul(Tanh(self.cout), ogate)


class LSTMTrainGraph(Graph):
    def __init__(self, num_char, hidden_dim):
        Graph.__init__(self, LogLoss(), SGD(eta=0.5, decay=0.95, gc=10))
        
        self.hidden_dim = hidden_dim
        self.num_char = num_char
        
        self.C2V = self.param_of((num_char, hidden_dim))
        self.wf = self.param_of((2 * hidden_dim, hidden_dim))
        self.bf = self.param_of((hidden_dim), Zero())
        self.wi = self.param_of((2 * hidden_dim, hidden_dim))
        self.bi = self.param_of((hidden_dim), Zero())
        self.wc = self.param_of((2 * hidden_dim, hidden_dim))
        self.bc = self.param_of((hidden_dim), Zero())
        self.wo = self.param_of((2 * hidden_dim, hidden_dim))
        self.bo = self.param_of((hidden_dim), Zero())
        self.V = self.param_of((hidden_dim, num_char))    
        
        self.h0 = self.input()
        self.c0 = self.input()
        
    def build(self, batch):
        data = batch.data
        B = data.shape[0]
        T = data.shape[1]
        hidden_dim = self.hidden_dim
    
        self.h0.value = np.zeros((B, hidden_dim))
        self.c0.value = np.zeros((B, hidden_dim))
        
        outputs = []
        for t in range(T - 1):
            x = self.input()
            x.value = data[:, t]
            wordvec = Embed(x, self.C2V)
            h = self.h0
            c = self.c0
            
            cell = LSTMCell(self.wf, self.bf,
                            self.wi, self.bi,
                            self.wc, self.bc, self.wo, self.bo, wordvec, h, c)
            
            outputs.append(SoftMax(Dot(cell.hout, self.V)))
            
            h = cell.hout
            c = cell.cout
        
        self.output(Collect(outputs))
        self.expect(data[:, 1:T])
        
class LSTMPredictGraph(LSTMTrainGraph):
    def __init__(self, num_char, hidden_dim):
        LSTMTrainGraph.__init__(self, num_char, hidden_dim)
        # No need for loss function
        self.loss = None
            
    def build(self, prefix, expect_length):
        prefix_len = prefix.shape()[0]
        hidden_dim = self.hidden_dim
    
        self.h0.value = np.zeros((1, hidden_dim))
        self.c0.value = np.zeros((1, hidden_dim))
        self.predicts = []
        for t in range(expect_length):
            h = self.h0
            c = self.c0
            if t < prefix_len:
                x = self.input()
                x.value = prefix[t]
            else:
                x = ArgMax(SoftMax(Dot(h, self.V)))
            self.predicts.append(x)
            wordvec = Embed(x, self.C2V)
            
            cell = LSTMCell(self.wf, self.bf,
                            self.wi, self.bi,
                            self.wc, self.bc, self.wo, self.bo, wordvec, h, c)
            
            h = cell.hout
            c = cell.cout
        
        